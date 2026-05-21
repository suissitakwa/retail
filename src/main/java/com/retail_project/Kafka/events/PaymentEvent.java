package com.retail_project.Kafka.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEvent {
    private Integer orderId;
    private Integer customerId;
    private String paymentIntentId;
    private String status;
    private BigDecimal amount;
}
