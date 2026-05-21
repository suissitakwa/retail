package com.retail_project.Kafka.consumers;

import com.retail_project.Kafka.events.PaymentEvent;
import com.retail_project.notification.Notification;
import com.retail_project.notification.NotificationRepository;
import com.retail_project.order.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentConsumer {

    private final NotificationRepository notificationRepository;
    private final OrderRepository orderRepository;

    @KafkaListener(topics = "payment.processed", groupId = "retail-backend",
                   containerFactory = "kafkaListenerContainerFactory")
    @Transactional
    public void onPaymentProcessed(PaymentEvent event) {
        log.info("Received payment.processed event for orderId={}, status={}", event.getOrderId(), event.getStatus());

        orderRepository.findById(event.getOrderId()).ifPresent(order -> {
            String message = "PAID".equalsIgnoreCase(event.getStatus())
                    ? "Payment confirmed for order #" + order.getReference() + ". Amount: $" + event.getAmount()
                    : "Payment failed for order #" + order.getReference() + ". Please try again.";

            Notification notification = Notification.builder()
                    .order(order)
                    .type("PAYMENT_" + event.getStatus())
                    .message(message)
                    .isRead(false)
                    .build();
            notificationRepository.save(notification);
            log.info("Saved PAYMENT_{} notification for orderId={}", event.getStatus(), event.getOrderId());
        });
    }
}
