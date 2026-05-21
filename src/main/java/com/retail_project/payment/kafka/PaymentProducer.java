package com.retail_project.payment.kafka;

import com.retail_project.Kafka.events.PaymentEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentProducer {

    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    public void sendPaymentProcessedEvent(PaymentEvent event) {
        kafkaTemplate.send("payment.processed", event);
    }
}
