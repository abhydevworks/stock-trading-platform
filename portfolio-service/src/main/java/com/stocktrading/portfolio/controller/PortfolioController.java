package com.stocktrading.portfolio.controller;

import com.stocktrading.common.dto.ApiResponse;
import com.stocktrading.portfolio.dto.AccountDTO;
import com.stocktrading.portfolio.dto.HoldingDTO;
import com.stocktrading.portfolio.service.PortfolioService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/portfolio")
@Slf4j
public class PortfolioController {

    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<AccountDTO>> getPortfolio(
            @RequestHeader("X-User-Id") UUID userId) {
        log.info("Fetch portfolio for user: {}", userId);
        AccountDTO portfolio = portfolioService.getPortfolio(userId);
        return ResponseEntity.ok(ApiResponse.success(portfolio));
    }

    @GetMapping("/holdings")
    public ResponseEntity<ApiResponse<List<HoldingDTO>>> getHoldings(
            @RequestHeader("X-User-Id") UUID userId) {
        log.info("Fetch holdings for user: {}", userId);
        List<HoldingDTO> holdings = portfolioService.getHoldings(userId);
        return ResponseEntity.ok(ApiResponse.success(holdings));
    }

    @GetMapping("/holdings/{symbol}")
    public ResponseEntity<ApiResponse<HoldingDTO>> getHolding(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable String symbol) {
        log.info("Fetch holding {} for user: {}", symbol, userId);
        HoldingDTO holding = portfolioService.getHolding(userId, symbol);
        return ResponseEntity.ok(ApiResponse.success(holding));
    }
}
