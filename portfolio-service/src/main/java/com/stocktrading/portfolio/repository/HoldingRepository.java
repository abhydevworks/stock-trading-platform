package com.stocktrading.portfolio.repository;

import com.stocktrading.portfolio.model.Holding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HoldingRepository extends JpaRepository<Holding, UUID> {
    List<Holding> findByAccountId(UUID accountId);
    Optional<Holding> findByAccountIdAndSymbol(UUID accountId, String symbol);
}
