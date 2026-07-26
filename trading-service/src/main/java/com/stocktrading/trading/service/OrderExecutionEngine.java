package com.stocktrading.trading.service;

import com.stocktrading.trading.model.Order;
import com.stocktrading.trading.model.Trade;
import com.stocktrading.trading.repository.OrderRepository;
import com.stocktrading.trading.repository.TradeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
public class OrderExecutionEngine {

    private final OrderRepository orderRepository;
    private final TradeRepository tradeRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final static BigDecimal COMMISSION_RATE = BigDecimal.valueOf(0.001); // 0.1%

    public OrderExecutionEngine(OrderRepository orderRepository, TradeRepository tradeRepository, 
                               RedisTemplate<String, Object> redisTemplate) {
        this.orderRepository = orderRepository;
        this.tradeRepository = tradeRepository;
        this.redisTemplate = redisTemplate;
    }

    @Async
    public void executeOrder(Order order) {
        log.info("Starting execution for order: {}", order.getId());
        
        try {
            // Get current price from Redis
            Double currentPrice = getCurrentPrice(order.getSymbol());
            
            if (currentPrice == null) {
                log.warn("No price data available for symbol: {}", order.getSymbol());
                updateOrderStatus(order, Order.OrderStatus.REJECTED);
                return;
            }

            // Execute based on order type
            switch (order.getOrderType()) {
                case MARKET:
                    executeMarketOrder(order, currentPrice);
                    break;
                case LIMIT:
                    executeLimitOrder(order, currentPrice);
                    break;
                case STOP_LOSS:
                    executeStopLossOrder(order, currentPrice);
                    break;
                case STOP_LIMIT:
                    executeStopLimitOrder(order, currentPrice);
                    break;
                default:
                    log.warn("Unknown order type: {}", order.getOrderType());
                    updateOrderStatus(order, Order.OrderStatus.REJECTED);
            }
        } catch (Exception e) {
            log.error("Error executing order: {}", order.getId(), e);
            updateOrderStatus(order, Order.OrderStatus.REJECTED);
        }
    }

    @Transactional
    private void executeMarketOrder(Order order, Double currentPrice) {
        log.info("Executing MARKET order: {}", order.getId());
        
        BigDecimal executionPrice = BigDecimal.valueOf(currentPrice);
        BigDecimal totalCost = order.getQuantity().multiply(executionPrice);
        BigDecimal commission = totalCost.multiply(COMMISSION_RATE);
        BigDecimal finalCost = totalCost.add(commission);

        // Create trade
        Trade trade = Trade.builder()
                .orderId(order.getId())
                .accountId(order.getAccountId())
                .symbol(order.getSymbol())
                .side(order.getSide())
                .quantity(order.getQuantity())
                .price(executionPrice)
                .totalCost(finalCost)
                .commission(commission)
                .createdAt(LocalDateTime.now())
                .build();

        tradeRepository.save(trade);
        log.info("Trade executed: {} - {} shares at {}", order.getId(), order.getQuantity(), executionPrice);

        // Update order status
        order.setStatus(Order.OrderStatus.FILLED);
        order.setFilledQuantity(order.getQuantity());
        order.setAverageFillPrice(executionPrice);
        order.setTotalCost(finalCost);
        order.setCommission(commission);
        order.setExecutedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
    }

    @Transactional
    private void executeLimitOrder(Order order, Double currentPrice) {
        log.info("Processing LIMIT order: {}", order.getId());
        
        BigDecimal limitPrice = order.getPrice();
        BigDecimal currentBigDecimal = BigDecimal.valueOf(currentPrice);

        // Check if limit condition is met
        boolean canExecute = false;
        if (order.getSide() == Order.Side.BUY && currentBigDecimal.compareTo(limitPrice) <= 0) {
            canExecute = true;
        } else if (order.getSide() == Order.Side.SELL && currentBigDecimal.compareTo(limitPrice) >= 0) {
            canExecute = true;
        }

        if (canExecute) {
            executeMarketOrder(order, limitPrice.doubleValue());
        } else {
            log.info("Limit order {} waiting to be matched", order.getId());
            order.setStatus(Order.OrderStatus.ACCEPTED);
            order.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(order);
            // Store order in Redis for matching engine
            redisTemplate.opsForList().leftPush("limit_orders:" + order.getSymbol(), order.getId().toString());
        }
    }

    @Transactional
    private void executeStopLossOrder(Order order, Double currentPrice) {
        log.info("Processing STOP_LOSS order: {}", order.getId());
        
        BigDecimal stopPrice = order.getStopPrice();
        BigDecimal currentBigDecimal = BigDecimal.valueOf(currentPrice);

        // For SELL stop loss: execute when price drops to stop price
        if (currentBigDecimal.compareTo(stopPrice) <= 0) {
            executeMarketOrder(order, currentPrice);
        } else {
            log.info("Stop loss order {} waiting for trigger price", order.getId());
            order.setStatus(Order.OrderStatus.ACCEPTED);
            order.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(order);
            // Store order in Redis for monitoring
            redisTemplate.opsForList().leftPush("stop_loss_orders:" + order.getSymbol(), order.getId().toString());
        }
    }

    @Transactional
    private void executeStopLimitOrder(Order order, Double currentPrice) {
        log.info("Processing STOP_LIMIT order: {}", order.getId());
        // Combine stop and limit logic
        BigDecimal stopPrice = order.getStopPrice();
        BigDecimal limitPrice = order.getPrice();
        BigDecimal currentBigDecimal = BigDecimal.valueOf(currentPrice);

        // Check if stop price is triggered
        if (currentBigDecimal.compareTo(stopPrice) <= 0 && currentBigDecimal.compareTo(limitPrice) <= 0) {
            executeMarketOrder(order, limitPrice.doubleValue());
        } else {
            order.setStatus(Order.OrderStatus.ACCEPTED);
            order.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(order);
        }
    }

    @Transactional
    private void updateOrderStatus(Order order, Order.OrderStatus status) {
        order.setStatus(status);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
    }

    private Double getCurrentPrice(String symbol) {
        // Retrieve current price from Redis cache
        Object price = redisTemplate.opsForValue().get("price:" + symbol);
        if (price != null) {
            return Double.parseDouble(price.toString());
        }
        return null;
    }
}
