package com.stocktrading.trading.repository;

import com.stocktrading.trading.model.Trade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TradeRepository extends JpaRepository<Trade, UUID> {
    Page<Trade> findByAccountId(UUID accountId, Pageable pageable);
    List<Trade> findBySymbol(String symbol);
    List<Trade> findByAccountIdAndSymbol(UUID accountId, String symbol);
}
