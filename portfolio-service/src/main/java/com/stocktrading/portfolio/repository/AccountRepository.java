package com.stocktrading.portfolio.repository;

import com.stocktrading.portfolio.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {
    Optional<Account> findByUserId(UUID userId);
    Optional<Account> findByAccountNumber(String accountNumber);
}
