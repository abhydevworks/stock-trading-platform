package com.stocktrading.marketdata.repository;

import com.stocktrading.marketdata.model.TechnicalIndicator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TechnicalIndicatorRepository extends JpaRepository<TechnicalIndicator, UUID> {
    Optional<TechnicalIndicator> findTopBySymbolOrderByTimestampDesc(String symbol);
}
