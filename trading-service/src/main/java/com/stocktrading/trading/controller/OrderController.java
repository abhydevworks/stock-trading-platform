package com.stocktrading.trading.controller;

import com.stocktrading.common.dto.ApiResponse;
import com.stocktrading.trading.dto.CreateOrderRequest;
import com.stocktrading.trading.dto.OrderDTO;
import com.stocktrading.trading.service.OrderService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@Slf4j
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OrderDTO>> createOrder(
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody CreateOrderRequest request) {
        log.info("Create order request from user: {} for symbol: {}", userId, request.getSymbol());
        OrderDTO order = orderService.createOrder(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order created successfully", order));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderDTO>> getOrder(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID orderId) {
        log.info("Fetch order {} for user: {}", orderId, userId);
        OrderDTO order = orderService.getOrder(orderId);
        return ResponseEntity.ok(ApiResponse.success(order));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<OrderDTO>>> getOrders(
            @RequestHeader("X-User-Id") UUID userId,
            Pageable pageable) {
        log.info("Fetch orders for user: {}", userId);
        Page<OrderDTO> orders = orderService.getAccountOrders(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderDTO>> cancelOrder(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID orderId) {
        log.info("Cancel order {} for user: {}", orderId, userId);
        OrderDTO order = orderService.cancelOrder(orderId);
        return ResponseEntity.ok(ApiResponse.success("Order cancelled successfully", order));
    }
}
