package com.stocktrading.notification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stocktrading.notification.model.Notification;
import com.stocktrading.notification.repository.NotificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final JavaMailSender mailSender;
    private final ObjectMapper objectMapper;

    public NotificationService(NotificationRepository notificationRepository, 
                              JavaMailSender mailSender,
                              ObjectMapper objectMapper) {
        this.notificationRepository = notificationRepository;
        this.mailSender = mailSender;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "trade-events", groupId = "notification-service-group")
    @Transactional
    public void consumeTradeEvent(String message) {
        try {
            log.debug("Received trade event: {}", message);
            Map<String, Object> eventData = objectMapper.readValue(message, Map.class);
            
            String eventType = (String) eventData.get("type");
            String userId = (String) eventData.get("userId");
            String orderId = (String) eventData.get("orderId");
            String symbol = (String) eventData.get("symbol");
            
            Notification notification = createTradeNotification(eventType, userId, orderId, symbol);
            if (notification != null) {
                sendNotification(notification);
            }
        } catch (Exception e) {
            log.error("Error processing trade event", e);
        }
    }

    @Transactional
    public void sendNotification(Notification notification) {
        log.info("Sending notification to user: {} type: {}", notification.getUserId(), notification.getType());
        
        try {
            // For now, sending email notifications
            if (notification.getRecipient() != null) {
                sendEmailNotification(notification);
            }
            
            notification.setStatus(Notification.NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
        } catch (Exception e) {
            log.error("Error sending notification", e);
            notification.setStatus(Notification.NotificationStatus.FAILED);
        }
        
        notificationRepository.save(notification);
    }

    private void sendEmailNotification(Notification notification) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(notification.getRecipient());
            message.setSubject(notification.getTitle());
            message.setText(notification.getMessage());
            message.setFrom("noreply@stocktrading.com");
            
            mailSender.send(message);
            log.info("Email sent successfully to: {}", notification.getRecipient());
        } catch (Exception e) {
            log.error("Error sending email to: {}", notification.getRecipient(), e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    private Notification createTradeNotification(String eventType, String userId, String orderId, String symbol) {
        Notification.NotificationType notificationType;
        String title;
        String message;
        
        switch (eventType) {
            case "ORDER_PLACED":
                notificationType = Notification.NotificationType.ORDER_PLACED;
                title = "Order Placed";
                message = String.format("Your order for %s (Order ID: %s) has been placed.", symbol, orderId);
                break;
            case "ORDER_FILLED":
                notificationType = Notification.NotificationType.ORDER_FILLED;
                title = "Order Filled";
                message = String.format("Your order for %s (Order ID: %s) has been filled.", symbol, orderId);
                break;
            case "ORDER_CANCELLED":
                notificationType = Notification.NotificationType.ORDER_CANCELLED;
                title = "Order Cancelled";
                message = String.format("Your order for %s (Order ID: %s) has been cancelled.", symbol, orderId);
                break;
            default:
                return null;
        }
        
        return Notification.builder()
                .userId(java.util.UUID.fromString(userId))
                .type(notificationType)
                .title(title)
                .message(message)
                .channel("EMAIL")
                .status(Notification.NotificationStatus.PENDING)
                .build();
    }
}
