package com.stocktrading.analytics.repository;

import com.stocktrading.analytics.model.TradeAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TradeAnalyticsRepository extends JpaRepository<TradeAnalytics, UUID> {
    Optional<TradeAnalytics> findTopByUserIdOrderByCalculatedAtDesc(UUID userId);
}
