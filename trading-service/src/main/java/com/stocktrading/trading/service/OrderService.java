package com.stocktrading.trading.service;

import com.stocktrading.common.exception.BusinessException;
import com.stocktrading.common.exception.ResourceNotFoundException;
import com.stocktrading.trading.dto.CreateOrderRequest;
import com.stocktrading.trading.dto.OrderDTO;
import com.stocktrading.trading.model.Order;
import com.stocktrading.trading.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderExecutionEngine executionEngine;

    public OrderService(OrderRepository orderRepository, OrderExecutionEngine executionEngine) {
        this.orderRepository = orderRepository;
        this.executionEngine = executionEngine;
    }

    @Transactional
    public OrderDTO createOrder(UUID accountId, CreateOrderRequest request) {
        log.info("Creating order for account {} - Symbol: {}, Side: {}, Quantity: {}", 
                accountId, request.getSymbol(), request.getSide(), request.getQuantity());

        // Validate order
        validateOrder(request);

        // Create order entity
        Order order = Order.builder()
                .accountId(accountId)
                .symbol(request.getSymbol())
                .orderType(Order.OrderType.valueOf(request.getOrderType()))
                .side(Order.Side.valueOf(request.getSide()))
                .quantity(request.getQuantity())
                .price(request.getPrice())
                .stopPrice(request.getStopPrice())
                .status(Order.OrderStatus.PENDING)
                .filledQuantity(BigDecimal.ZERO)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Order savedOrder = orderRepository.save(order);
        log.info("Order created with id: {}", savedOrder.getId());

        // Trigger execution engine
        executionEngine.executeOrder(savedOrder);

        return convertToDTO(savedOrder);
    }

    @Transactional
    public OrderDTO cancelOrder(UUID orderId) {
        log.info("Cancelling order: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        if (order.getStatus() == Order.OrderStatus.FILLED || 
            order.getStatus() == Order.OrderStatus.CANCELLED ||
            order.getStatus() == Order.OrderStatus.REJECTED) {
            throw new BusinessException("CANNOT_CANCEL", "Cannot cancel order with status: " + order.getStatus());
        }

        order.setStatus(Order.OrderStatus.CANCELLED);
        order.setUpdatedAt(LocalDateTime.now());
        Order cancelledOrder = orderRepository.save(order);

        return convertToDTO(cancelledOrder);
    }

    public OrderDTO getOrder(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
        return convertToDTO(order);
    }

    public Page<OrderDTO> getAccountOrders(UUID accountId, Pageable pageable) {
        return orderRepository.findByAccountId(accountId, pageable)
                .map(this::convertToDTO);
    }

    @Transactional
    public OrderDTO updateOrderStatus(UUID orderId, Order.OrderStatus status, BigDecimal filledQuantity, BigDecimal averageFillPrice) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        order.setStatus(status);
        order.setFilledQuantity(filledQuantity);
        order.setAverageFillPrice(averageFillPrice);
        order.setTotalCost(filledQuantity.multiply(averageFillPrice));
        order.setUpdatedAt(LocalDateTime.now());

        if (status == Order.OrderStatus.FILLED || status == Order.OrderStatus.PARTIALLY_FILLED) {
            order.setExecutedAt(LocalDateTime.now());
        }

        Order updatedOrder = orderRepository.save(order);
        return convertToDTO(updatedOrder);
    }

    private void validateOrder(CreateOrderRequest request) {
        if (request.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("INVALID_QUANTITY", "Quantity must be greater than 0");
        }

        if ("LIMIT".equals(request.getOrderType()) && request.getPrice() == null) {
            throw new BusinessException("INVALID_ORDER", "Price is required for LIMIT orders");
        }

        if ("STOP_LOSS".equals(request.getOrderType()) && request.getStopPrice() == null) {
            throw new BusinessException("INVALID_ORDER", "Stop price is required for STOP_LOSS orders");
        }
    }

    private OrderDTO convertToDTO(Order order) {
        return OrderDTO.builder()
                .id(order.getId())
                .accountId(order.getAccountId())
                .symbol(order.getSymbol())
                .orderType(order.getOrderType().toString())
                .side(order.getSide().toString())
                .quantity(order.getQuantity())
                .price(order.getPrice())
                .stopPrice(order.getStopPrice())
                .status(order.getStatus().toString())
                .filledQuantity(order.getFilledQuantity())
                .averageFillPrice(order.getAverageFillPrice())
                .totalCost(order.getTotalCost())
                .commission(order.getCommission())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .executedAt(order.getExecutedAt())
                .build();
    }
}
