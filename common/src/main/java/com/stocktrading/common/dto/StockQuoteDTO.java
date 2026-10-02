package com.stocktrading.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockQuoteDTO {
    private String symbol;
    private String name;
    private BigDecimal price;
    private BigDecimal change;
    private BigDecimal changePercent;
    private BigDecimal high;
    private BigDecimal low;
    private BigDecimal open;
    private Long volume;
    private LocalDateTime timestamp;
}
