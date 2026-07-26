package com.stocktrading.marketdata.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "technical_indicators")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TechnicalIndicator {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String symbol;

    private BigDecimal sma20;
    private BigDecimal sma50;
    private BigDecimal ema12;
    private BigDecimal ema26;
    private BigDecimal macd;
    private BigDecimal rsi;
    private BigDecimal bollinger_upper;
    private BigDecimal bollinger_middle;
    private BigDecimal bollinger_lower;

    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;
}
