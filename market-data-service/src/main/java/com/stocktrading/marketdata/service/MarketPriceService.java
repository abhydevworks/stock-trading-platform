package com.stocktrading.marketdata.service;

import com.stocktrading.common.exception.ResourceNotFoundException;
import com.stocktrading.marketdata.dto.MarketPriceDTO;
import com.stocktrading.marketdata.model.MarketPrice;
import com.stocktrading.marketdata.repository.MarketPriceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MarketPriceService {

    private final MarketPriceRepository marketPriceRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public MarketPriceService(MarketPriceRepository marketPriceRepository, 
                             RedisTemplate<String, Object> redisTemplate,
                             KafkaTemplate<String, String> kafkaTemplate) {
        this.marketPriceRepository = marketPriceRepository;
        this.redisTemplate = redisTemplate;
        this.kafkaTemplate = kafkaTemplate;
    }

    public MarketPriceDTO getCurrentPrice(String symbol) {
        log.info("Fetching current price for symbol: {}", symbol);
        
        // Try to get from Redis cache first
        Object cachedPrice = redisTemplate.opsForValue().get("price:" + symbol);
        if (cachedPrice != null) {
            log.debug("Found price in cache for symbol: {}", symbol);
            return (MarketPriceDTO) cachedPrice;
        }
        
        // Get from database
        MarketPrice price = marketPriceRepository.findTopBySymbolOrderByTimestampDesc(symbol)
                .orElseThrow(() -> new ResourceNotFoundException("Price data not found for symbol: " + symbol));
        
        MarketPriceDTO dto = convertToDTO(price);
        
        // Cache for 1 minute
        redisTemplate.opsForValue().set("price:" + symbol, dto);
        
        return dto;
    }

    public List<MarketPriceDTO> getPriceHistory(String symbol, MarketPrice.TimeFrame timeFrame, int limit) {
        log.info("Fetching price history for symbol: {} timeFrame: {} limit: {}", symbol, timeFrame, limit);
        
        List<MarketPrice> prices = marketPriceRepository.findBySymbolAndTimeFrameOrderByTimestampDesc(symbol, timeFrame)
                .stream()
                .limit(limit)
                .collect(Collectors.toList());
        
        return prices.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Page<MarketPriceDTO> getPricesBySymbol(String symbol, Pageable pageable) {
        log.info("Fetching prices for symbol: {} page: {}", symbol, pageable);
        
        return marketPriceRepository.findBySymbol(symbol, pageable)
                .map(this::convertToDTO);
    }

    @Transactional
    public MarketPriceDTO recordPrice(String symbol, BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close, Long volume) {
        log.info("Recording price for symbol: {} close: {}", symbol, close);
        
        MarketPrice price = MarketPrice.builder()
                .symbol(symbol)
                .open(open)
                .high(high)
                .low(low)
                .close(close)
                .volume(volume)
                .timestamp(LocalDateTime.now())
                .timeFrame(MarketPrice.TimeFrame.ONE_MINUTE)
                .build();
        
        MarketPrice savedPrice = marketPriceRepository.save(price);
        
        // Update Redis cache
        MarketPriceDTO dto = convertToDTO(savedPrice);
        redisTemplate.opsForValue().set("price:" + symbol, dto);
        
        // Publish to Kafka
        publishPriceUpdate(symbol, close);
        
        return dto;
    }

    private void publishPriceUpdate(String symbol, BigDecimal price) {
        try {
            String message = String.format("{\"symbol\":\"%s\",\"price\":%s}", symbol, price);
            kafkaTemplate.send("market-prices", message);
            log.debug("Published price update for symbol: {} to Kafka", symbol);
        } catch (Exception e) {
            log.error("Error publishing price update for symbol: {}", symbol, e);
        }
    }

    private MarketPriceDTO convertToDTO(MarketPrice price) {
        BigDecimal change = price.getClose().subtract(price.getOpen());
        BigDecimal changePercent = price.getOpen().compareTo(BigDecimal.ZERO) > 0 ?
                change.divide(price.getOpen(), 4, RoundingMode.HALF_UP).multiply(new BigDecimal(100)) :
                BigDecimal.ZERO;
        
        return MarketPriceDTO.builder()
                .id(price.getId())
                .symbol(price.getSymbol())
                .open(price.getOpen())
                .high(price.getHigh())
                .low(price.getLow())
                .close(price.getClose())
                .volume(price.getVolume())
                .timeFrame(price.getTimeFrame().toString())
                .timestamp(price.getTimestamp())
                .change(change)
                .changePercent(changePercent)
                .build();
    }
}
