package com.retail_project.Kafka.consumers;

import com.retail_project.Kafka.events.OrderEvent;
import com.retail_project.Kafka.events.OrderEventItem;
import com.retail_project.inventory.InventoryRepository;
import com.retail_project.notification.Notification;
import com.retail_project.notification.NotificationRepository;
import com.retail_project.order.Order;
import com.retail_project.order.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderConsumer {

    private final InventoryRepository inventoryRepository;
    private final NotificationRepository notificationRepository;
    private final OrderRepository orderRepository;

    @KafkaListener(topics = "order.created", groupId = "retail-backend",
                   containerFactory = "kafkaListenerContainerFactory")
    @Transactional
    public void onOrderCreated(OrderEvent event) {
        log.info("Received order.created event for orderId={}", event.getOrderId());

        // Decrement inventory for each item in the order
        if (event.getItems() != null) {
            for (OrderEventItem item : event.getItems()) {
                int updated = inventoryRepository.decrementStock(item.getProductId(), item.getQuantity());
                if (updated == 0) {
                    log.warn("Insufficient stock for productId={}, requested qty={}",
                            item.getProductId(), item.getQuantity());
                } else {
                    log.info("Decremented stock: productId={}, qty={}", item.getProductId(), item.getQuantity());
                }
            }
        }

        // Persist ORDER_PLACED notification
        orderRepository.findById(event.getOrderId()).ifPresent(order -> {
            Notification notification = Notification.builder()
                    .order(order)
                    .type("ORDER_PLACED")
                    .message("Your order #" + order.getReference() + " has been placed successfully.")
                    .isRead(false)
                    .build();
            notificationRepository.save(notification);
            log.info("Saved ORDER_PLACED notification for orderId={}", event.getOrderId());
        });
    }
}
