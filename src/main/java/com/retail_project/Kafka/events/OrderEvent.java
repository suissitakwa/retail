package com.retail_project.Kafka.events;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
@Getter
@Setter
@RequiredArgsConstructor
public class OrderEvent {

        private Integer orderId;
        private BigDecimal amount;




}
