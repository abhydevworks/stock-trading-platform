package com.stocktrading.trading.dto;

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
public class OrderDTO {
    private UUID id;
    private UUID accountId;
    private String symbol;
    private String orderType;
    private String side;
    private BigDecimal quantity;
    private BigDecimal price;
    private BigDecimal stopPrice;
    private String status;
    private BigDecimal filledQuantity;
    private BigDecimal averageFillPrice;
    private BigDecimal totalCost;
    private BigDecimal commission;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime executedAt;
}
