package com.stocktrading.payment.service;

import com.stocktrading.common.exception.BusinessException;
import com.stocktrading.common.exception.ResourceNotFoundException;
import com.stocktrading.payment.model.Wallet;
import com.stocktrading.payment.model.Transaction;
import com.stocktrading.payment.repository.WalletRepository;
import com.stocktrading.payment.repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
public class PaymentService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public PaymentService(WalletRepository walletRepository, TransactionRepository transactionRepository,
                         KafkaTemplate<String, String> kafkaTemplate) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    public void deposit(UUID userId, BigDecimal amount, String paymentMethod) {
        log.info("Processing deposit for user: {} amount: {} method: {}", userId, amount, paymentMethod);
        
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for user: " + userId));
        
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("INVALID_AMOUNT", "Amount must be greater than 0");
        }
        
        Transaction transaction = Transaction.builder()
                .walletId(wallet.getId())
                .type(Transaction.TransactionType.DEPOSIT)
                .amount(amount)
                .status(Transaction.TransactionStatus.PENDING)
                .paymentMethod(paymentMethod)
                .description("Deposit via " + paymentMethod)
                .createdAt(LocalDateTime.now())
                .build();
        
        Transaction savedTransaction = transactionRepository.save(transaction);
        
        // Update wallet balance
        wallet.setBalance(wallet.getBalance().add(amount));
        wallet.setUpdatedAt(LocalDateTime.now());
        walletRepository.save(wallet);
        
        // Mark transaction as completed
        savedTransaction.setStatus(Transaction.TransactionStatus.COMPLETED);
        savedTransaction.setCompletedAt(LocalDateTime.now());
        transactionRepository.save(savedTransaction);
        
        // Publish event
        publishPaymentEvent("DEPOSIT_COMPLETED", userId, amount);
    }

    @Transactional
    public void withdraw(UUID userId, BigDecimal amount) {
        log.info("Processing withdrawal for user: {} amount: {}", userId, amount);
        
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for user: " + userId));
        
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("INVALID_AMOUNT", "Amount must be greater than 0");
        }
        
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new BusinessException("INSUFFICIENT_FUNDS", "Insufficient balance for withdrawal");
        }
        
        Transaction transaction = Transaction.builder()
                .walletId(wallet.getId())
                .type(Transaction.TransactionType.WITHDRAWAL)
                .amount(amount)
                .status(Transaction.TransactionStatus.PENDING)
                .description("Withdrawal")
                .createdAt(LocalDateTime.now())
                .build();
        
        Transaction savedTransaction = transactionRepository.save(transaction);
        
        // Update wallet balance
        wallet.setBalance(wallet.getBalance().subtract(amount));
        wallet.setUpdatedAt(LocalDateTime.now());
        walletRepository.save(wallet);
        
        // Mark transaction as completed
        savedTransaction.setStatus(Transaction.TransactionStatus.COMPLETED);
        savedTransaction.setCompletedAt(LocalDateTime.now());
        transactionRepository.save(savedTransaction);
        
        // Publish event
        publishPaymentEvent("WITHDRAWAL_COMPLETED", userId, amount);
    }

    public Wallet getWallet(UUID userId) {
        return walletRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for user: " + userId));
    }

    private void publishPaymentEvent(String eventType, UUID userId, BigDecimal amount) {
        try {
            String message = String.format("{\"type\":\"%s\",\"userId\":\"%s\",\"amount\":%s}", 
                    eventType, userId, amount);
            kafkaTemplate.send("payment-events", message);
            log.debug("Published payment event: {}", eventType);
        } catch (Exception e) {
            log.error("Error publishing payment event", e);
        }
    }
}
