package com.stocktrading.trading.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID accountId;

    @Column(nullable = false)
    private String symbol;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderType orderType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Side side;

    @Column(nullable = false)
    private BigDecimal quantity;

    private BigDecimal price;
    private BigDecimal stopPrice;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    @Builder.Default
    private BigDecimal filledQuantity = BigDecimal.ZERO;

    private BigDecimal averageFillPrice;
    private BigDecimal totalCost;
    private BigDecimal commission;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    private LocalDateTime executedAt;

    public enum OrderType {
        MARKET, LIMIT, STOP_LOSS, STOP_LIMIT
    }

    public enum Side {
        BUY, SELL
    }

    public enum OrderStatus {
        PENDING, ACCEPTED, FILLED, PARTIALLY_FILLED, CANCELLED, REJECTED, EXPIRED
    }
}
