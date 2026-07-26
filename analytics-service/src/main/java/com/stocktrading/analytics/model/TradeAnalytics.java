package com.stocktrading.analytics.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "trade_analytics")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeAnalytics {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    private Long totalTrades;
    private Long winningTrades;
    private Long losingTrades;
    private BigDecimal winRate;
    private BigDecimal totalProfitLoss;
    private BigDecimal averageProfit;
    private BigDecimal averageLoss;
    private BigDecimal profitFactor;
    private BigDecimal sharpeRatio;
    private BigDecimal maxDrawdown;

    @Builder.Default
    private LocalDateTime calculatedAt = LocalDateTime.now();
}
