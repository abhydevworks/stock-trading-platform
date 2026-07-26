package com.stocktrading.marketdata.service;

import com.stocktrading.marketdata.model.TechnicalIndicator;
import com.stocktrading.marketdata.model.MarketPrice;
import com.stocktrading.marketdata.repository.TechnicalIndicatorRepository;
import com.stocktrading.marketdata.repository.MarketPriceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class TechnicalAnalysisService {

    private final MarketPriceRepository marketPriceRepository;
    private final TechnicalIndicatorRepository technicalIndicatorRepository;

    public TechnicalAnalysisService(MarketPriceRepository marketPriceRepository,
                                   TechnicalIndicatorRepository technicalIndicatorRepository) {
        this.marketPriceRepository = marketPriceRepository;
        this.technicalIndicatorRepository = technicalIndicatorRepository;
    }

    @Transactional
    public TechnicalIndicator calculateIndicators(String symbol) {
        log.info("Calculating technical indicators for symbol: {}", symbol);
        
        List<MarketPrice> prices = marketPriceRepository.findBySymbolAndTimeFrameOrderByTimestampDesc(
                symbol, MarketPrice.TimeFrame.ONE_DAY);
        
        if (prices.isEmpty()) {
            log.warn("No price data available for symbol: {}", symbol);
            return null;
        }
        
        TechnicalIndicator indicator = TechnicalIndicator.builder()
                .symbol(symbol)
                .timestamp(LocalDateTime.now())
                .sma20(calculateSMA(prices, 20))
                .sma50(calculateSMA(prices, 50))
                .rsi(calculateRSI(prices, 14))
                .build();
        
        return technicalIndicatorRepository.save(indicator);
    }

    private BigDecimal calculateSMA(List<MarketPrice> prices, int period) {
        if (prices.size() < period) {
            return null;
        }
        
        BigDecimal sum = BigDecimal.ZERO;
        for (int i = 0; i < period; i++) {
            sum = sum.add(prices.get(i).getClose());
        }
        
        return sum.divide(new BigDecimal(period), 6, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateRSI(List<MarketPrice> prices, int period) {
        if (prices.size() < period + 1) {
            return null;
        }
        
        BigDecimal gains = BigDecimal.ZERO;
        BigDecimal losses = BigDecimal.ZERO;
        
        for (int i = 0; i < period; i++) {
            BigDecimal change = prices.get(i).getClose().subtract(prices.get(i + 1).getClose());
            if (change.compareTo(BigDecimal.ZERO) > 0) {
                gains = gains.add(change);
            } else {
                losses = losses.add(change.abs());
            }
        }
        
        BigDecimal avgGain = gains.divide(new BigDecimal(period), 6, RoundingMode.HALF_UP);
        BigDecimal avgLoss = losses.divide(new BigDecimal(period), 6, RoundingMode.HALF_UP);
        
        if (avgLoss.compareTo(BigDecimal.ZERO) == 0) {
            return new BigDecimal(100);
        }
        
        BigDecimal rs = avgGain.divide(avgLoss, 6, RoundingMode.HALF_UP);
        BigDecimal rsi = new BigDecimal(100).subtract(
                new BigDecimal(100).divide(BigDecimal.ONE.add(rs), 6, RoundingMode.HALF_UP)
        );
        
        return rsi;
    }
}
