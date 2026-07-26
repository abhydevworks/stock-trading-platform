package com.stocktrading.notification.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String message;

    private String channel; // EMAIL, SMS, IN_APP

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private NotificationStatus status = NotificationStatus.PENDING;

    private String recipient;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime sentAt;

    public enum NotificationType {
        ORDER_PLACED, ORDER_FILLED, ORDER_CANCELLED, 
        PRICE_ALERT, PORTFOLIO_ALERT, TRADE_EXECUTED, 
        PAYMENT_RECEIVED, PAYMENT_FAILED
    }

    public enum NotificationStatus {
        PENDING, SENT, FAILED, BOUNCED
    }
}
