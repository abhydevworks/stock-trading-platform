package com.stocktrading.analytics.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stocktrading.analytics.model.TradeAnalytics;
import com.stocktrading.analytics.repository.TradeAnalyticsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class AnalyticsService {

    private final TradeAnalyticsRepository analyticsRepository;
    private final ObjectMapper objectMapper;

    public AnalyticsService(TradeAnalyticsRepository analyticsRepository, ObjectMapper objectMapper) {
        this.analyticsRepository = analyticsRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "trade-events", groupId = "analytics-service-group")
    @Transactional
    public void consumeTradeEvent(String message) {
        try {
            log.debug("Received trade event for analytics: {}", message);
            Map<String, Object> eventData = objectMapper.readValue(message, Map.class);
            
            String userId = (String) eventData.get("userId");
            String eventType = (String) eventData.get("type");
            
            if ("TRADE_EXECUTED".equals(eventType)) {
                updateAnalytics(UUID.fromString(userId));
            }
        } catch (Exception e) {
            log.error("Error processing trade event for analytics", e);
        }
    }

    @Transactional
    public void updateAnalytics(UUID userId) {
        log.info("Updating analytics for user: {}", userId);
        
        // Calculate analytics metrics
        TradeAnalytics analytics = TradeAnalytics.builder()
                .userId(userId)
                .totalTrades(0L)
                .winningTrades(0L)
                .losingTrades(0L)
                .winRate(BigDecimal.ZERO)
                .totalProfitLoss(BigDecimal.ZERO)
                .averageProfit(BigDecimal.ZERO)
                .averageLoss(BigDecimal.ZERO)
                .profitFactor(BigDecimal.ZERO)
                .sharpeRatio(BigDecimal.ZERO)
                .maxDrawdown(BigDecimal.ZERO)
                .calculatedAt(LocalDateTime.now())
                .build();
        
        analyticsRepository.save(analytics);
        log.info("Analytics updated for user: {}", userId);
    }

    public TradeAnalytics getAnalytics(UUID userId) {
        return analyticsRepository.findTopByUserIdOrderByCalculatedAtDesc(userId)
                .orElse(null);
    }
}
