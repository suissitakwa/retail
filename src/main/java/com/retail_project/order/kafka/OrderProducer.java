package com.retail_project.order.kafka;

import com.retail_project.Kafka.events.OrderEvent;
import lombok.AllArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class OrderProducer {

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;


    public void sendOrderCreatedEvent(OrderEvent event) {
        kafkaTemplate.send("order.created", event);
    }
}