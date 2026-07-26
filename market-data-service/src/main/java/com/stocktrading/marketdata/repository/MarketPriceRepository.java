package com.stocktrading.marketdata.repository;

import com.stocktrading.marketdata.model.MarketPrice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MarketPriceRepository extends JpaRepository<MarketPrice, UUID> {
    Optional<MarketPrice> findTopBySymbolOrderByTimestampDesc(String symbol);
    
    List<MarketPrice> findBySymbolAndTimeFrameOrderByTimestampDesc(String symbol, MarketPrice.TimeFrame timeFrame);
    
    @Query("SELECT mp FROM MarketPrice mp WHERE mp.symbol = ?1 AND mp.timestamp BETWEEN ?2 AND ?3 ORDER BY mp.timestamp DESC")
    List<MarketPrice> findPriceRange(String symbol, LocalDateTime startTime, LocalDateTime endTime);
    
    Page<MarketPrice> findBySymbol(String symbol, Pageable pageable);
}
