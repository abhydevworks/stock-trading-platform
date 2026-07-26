package com.stocktrading.portfolio.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stocktrading.portfolio.service.PortfolioService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Service
@Slf4j
public class MarketDataConsumer {

    private final PortfolioService portfolioService;
    private final ObjectMapper objectMapper;

    public MarketDataConsumer(PortfolioService portfolioService, ObjectMapper objectMapper) {
        this.portfolioService = portfolioService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "market-prices", groupId = "portfolio-service-group")
    public void consumeMarketPrice(String message) {
        try {
            log.debug("Received market price update: {}", message);
            Map<String, Object> priceData = objectMapper.readValue(message, Map.class);
            
            String symbol = (String) priceData.get("symbol");
            BigDecimal price = new BigDecimal(priceData.get("price").toString());
            
            portfolioService.updateHoldingPrices(symbol, price);
        } catch (Exception e) {
            log.error("Error processing market price update", e);
        }
    }
}
