package com.stocktrading.portfolio.service;

import com.stocktrading.common.exception.ResourceNotFoundException;
import com.stocktrading.portfolio.dto.AccountDTO;
import com.stocktrading.portfolio.dto.HoldingDTO;
import com.stocktrading.portfolio.model.Account;
import com.stocktrading.portfolio.model.Holding;
import com.stocktrading.portfolio.repository.AccountRepository;
import com.stocktrading.portfolio.repository.HoldingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PortfolioService {

    private final AccountRepository accountRepository;
    private final HoldingRepository holdingRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public PortfolioService(AccountRepository accountRepository, HoldingRepository holdingRepository,
                           RedisTemplate<String, Object> redisTemplate) {
        this.accountRepository = accountRepository;
        this.holdingRepository = holdingRepository;
        this.redisTemplate = redisTemplate;
    }

    public AccountDTO getPortfolio(UUID userId) {
        log.info("Fetching portfolio for user: {}", userId);
        Account account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found for user: " + userId));
        
        return convertAccountToDTO(account);
    }

    public List<HoldingDTO> getHoldings(UUID userId) {
        log.info("Fetching holdings for user: {}", userId);
        Account account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found for user: " + userId));
        
        List<Holding> holdings = holdingRepository.findByAccountId(account.getId());
        return holdings.stream()
                .map(this::convertHoldingToDTO)
                .collect(Collectors.toList());
    }

    public HoldingDTO getHolding(UUID userId, String symbol) {
        log.info("Fetching holding for user: {} symbol: {}", userId, symbol);
        Account account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found for user: " + userId));
        
        Holding holding = holdingRepository.findByAccountIdAndSymbol(account.getId(), symbol)
                .orElseThrow(() -> new ResourceNotFoundException("Holding not found for symbol: " + symbol));
        
        return convertHoldingToDTO(holding);
    }

    @Transactional
    public void addOrUpdateHolding(UUID userId, String symbol, BigDecimal quantity, BigDecimal price) {
        log.info("Adding/updating holding for user: {} symbol: {} quantity: {} price: {}", userId, symbol, quantity, price);
        
        Account account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found for user: " + userId));
        
        Holding existingHolding = holdingRepository.findByAccountIdAndSymbol(account.getId(), symbol).orElse(null);
        
        if (existingHolding != null) {
            // Update existing holding
            BigDecimal totalCost = existingHolding.getAveragePrice().multiply(existingHolding.getQuantity())
                    .add(price.multiply(quantity));
            BigDecimal newQuantity = existingHolding.getQuantity().add(quantity);
            BigDecimal newAveragePrice = totalCost.divide(newQuantity, 6, RoundingMode.HALF_UP);
            
            existingHolding.setQuantity(newQuantity);
            existingHolding.setAveragePrice(newAveragePrice);
            existingHolding.setUpdatedAt(LocalDateTime.now());
            holdingRepository.save(existingHolding);
        } else {
            // Create new holding
            Holding newHolding = Holding.builder()
                    .accountId(account.getId())
                    .symbol(symbol)
                    .quantity(quantity)
                    .averagePrice(price)
                    .updatedAt(LocalDateTime.now())
                    .build();
            holdingRepository.save(newHolding);
        }
        
        // Update account total invested
        updateAccountTotals(account.getId());
    }

    @Transactional
    public void updateHoldingPrices(String symbol, BigDecimal currentPrice) {
        log.info("Updating prices for symbol: {} price: {}", symbol, currentPrice);
        
        List<Holding> holdings = holdingRepository.findAll()
                .stream()
                .filter(h -> h.getSymbol().equals(symbol))
                .toList();
        
        for (Holding holding : holdings) {
            BigDecimal currentValue = holding.getQuantity().multiply(currentPrice);
            BigDecimal investedValue = holding.getQuantity().multiply(holding.getAveragePrice());
            BigDecimal unrealizedGainLoss = currentValue.subtract(investedValue);
            
            holding.setCurrentPrice(currentPrice);
            holding.setCurrentValue(currentValue);
            holding.setUnrealizedGainLoss(unrealizedGainLoss);
            holding.setUpdatedAt(LocalDateTime.now());
            holdingRepository.save(holding);
            
            // Update account totals
            updateAccountTotals(holding.getAccountId());
        }
    }

    @Transactional
    private void updateAccountTotals(UUID accountId) {
        List<Holding> holdings = holdingRepository.findByAccountId(accountId);
        
        BigDecimal portfolioValue = BigDecimal.ZERO;
        BigDecimal totalGainLoss = BigDecimal.ZERO;
        BigDecimal totalInvested = BigDecimal.ZERO;
        
        for (Holding holding : holdings) {
            if (holding.getCurrentValue() != null) {
                portfolioValue = portfolioValue.add(holding.getCurrentValue());
            }
            if (holding.getUnrealizedGainLoss() != null) {
                totalGainLoss = totalGainLoss.add(holding.getUnrealizedGainLoss());
            }
            totalInvested = totalInvested.add(holding.getQuantity().multiply(holding.getAveragePrice()));
        }
        
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + accountId));
        
        account.setTotalInvested(totalInvested);
        account.setTotalReturns(totalGainLoss);
        account.setUpdatedAt(LocalDateTime.now());
        accountRepository.save(account);
    }

    private AccountDTO convertAccountToDTO(Account account) {
        // Calculate portfolio totals
        List<Holding> holdings = holdingRepository.findByAccountId(account.getId());
        BigDecimal portfolioValue = BigDecimal.ZERO;
        BigDecimal totalGainLoss = BigDecimal.ZERO;
        
        for (Holding holding : holdings) {
            if (holding.getCurrentValue() != null) {
                portfolioValue = portfolioValue.add(holding.getCurrentValue());
                totalGainLoss = totalGainLoss.add(holding.getUnrealizedGainLoss() != null ? holding.getUnrealizedGainLoss() : BigDecimal.ZERO);
            }
        }
        
        BigDecimal gainLossPercentage = BigDecimal.ZERO;
        if (account.getTotalInvested().compareTo(BigDecimal.ZERO) > 0) {
            gainLossPercentage = totalGainLoss.divide(account.getTotalInvested(), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal(100));
        }
        
        return AccountDTO.builder()
                .id(account.getId())
                .userId(account.getUserId())
                .accountNumber(account.getAccountNumber())
                .accountType(account.getAccountType())
                .currency(account.getCurrency())
                .balance(account.getBalance())
                .availableBalance(account.getAvailableBalance())
                .totalInvested(account.getTotalInvested())
                .totalReturns(account.getTotalReturns())
                .portfolioValue(portfolioValue)
                .totalGainLoss(totalGainLoss)
                .gainLossPercentage(gainLossPercentage)
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }

    private HoldingDTO convertHoldingToDTO(Holding holding) {
        BigDecimal gainLossPercentage = BigDecimal.ZERO;
        if (holding.getAveragePrice().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal gainLoss = holding.getCurrentPrice() != null ? 
                    holding.getCurrentPrice().subtract(holding.getAveragePrice()) : BigDecimal.ZERO;
            gainLossPercentage = gainLoss.divide(holding.getAveragePrice(), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal(100));
        }
        
        return HoldingDTO.builder()
                .id(holding.getId())
                .symbol(holding.getSymbol())
                .quantity(holding.getQuantity())
                .averagePrice(holding.getAveragePrice())
                .currentPrice(holding.getCurrentPrice())
                .currentValue(holding.getCurrentValue())
                .unrealizedGainLoss(holding.getUnrealizedGainLoss())
                .gainLossPercentage(gainLossPercentage)
                .updatedAt(holding.getUpdatedAt())
                .build();
    }
}
