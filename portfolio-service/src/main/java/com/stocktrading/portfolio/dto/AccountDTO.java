package com.stocktrading.portfolio.dto;

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
public class AccountDTO {
    private UUID id;
    private UUID userId;
    private String accountNumber;
    private String accountType;
    private String currency;
    private BigDecimal balance;
    private BigDecimal availableBalance;
    private BigDecimal totalInvested;
    private BigDecimal totalReturns;
    private BigDecimal portfolioValue;
    private BigDecimal totalGainLoss;
    private BigDecimal gainLossPercentage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
