package com.stocktrading.marketdata.controller;

import com.stocktrading.common.dto.ApiResponse;
import com.stocktrading.marketdata.dto.MarketPriceDTO;
import com.stocktrading.marketdata.model.MarketPrice;
import com.stocktrading.marketdata.service.MarketPriceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/market")
@Slf4j
public class MarketDataController {

    private final MarketPriceService marketPriceService;

    public MarketDataController(MarketPriceService marketPriceService) {
        this.marketPriceService = marketPriceService;
    }

    @GetMapping("/{symbol}")
    public ResponseEntity<ApiResponse<MarketPriceDTO>> getCurrentPrice(
            @PathVariable String symbol) {
        log.info("Fetch current price for symbol: {}", symbol);
        MarketPriceDTO price = marketPriceService.getCurrentPrice(symbol);
        return ResponseEntity.ok(ApiResponse.success(price));
    }

    @GetMapping("/{symbol}/history")
    public ResponseEntity<ApiResponse<List<MarketPriceDTO>>> getPriceHistory(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "ONE_DAY") String timeFrame,
            @RequestParam(defaultValue = "100") int limit) {
        log.info("Fetch price history for symbol: {} timeFrame: {} limit: {}", symbol, timeFrame, limit);
        List<MarketPriceDTO> history = marketPriceService.getPriceHistory(
                symbol, MarketPrice.TimeFrame.valueOf(timeFrame), limit);
        return ResponseEntity.ok(ApiResponse.success(history));
    }

    @GetMapping("/{symbol}/page")
    public ResponseEntity<ApiResponse<Page<MarketPriceDTO>>> getPrices(
            @PathVariable String symbol,
            Pageable pageable) {
        log.info("Fetch prices for symbol: {} page: {}", symbol, pageable);
        Page<MarketPriceDTO> prices = marketPriceService.getPricesBySymbol(symbol, pageable);
        return ResponseEntity.ok(ApiResponse.success(prices));
    }

    @PostMapping("/{symbol}/record")
    public ResponseEntity<ApiResponse<MarketPriceDTO>> recordPrice(
            @PathVariable String symbol,
            @RequestParam BigDecimal open,
            @RequestParam BigDecimal high,
            @RequestParam BigDecimal low,
            @RequestParam BigDecimal close,
            @RequestParam Long volume) {
        log.info("Record price for symbol: {} close: {}", symbol, close);
        MarketPriceDTO price = marketPriceService.recordPrice(symbol, open, high, low, close, volume);
        return ResponseEntity.ok(ApiResponse.success("Price recorded", price));
    }
}
