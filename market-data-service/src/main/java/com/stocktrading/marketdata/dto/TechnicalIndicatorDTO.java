package com.stocktrading.marketdata.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TechnicalIndicatorDTO {
    private UUID id;
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
    private LocalDateTime timestamp;
}
