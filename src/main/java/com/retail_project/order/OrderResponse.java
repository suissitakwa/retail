package com.retail_project.order;

import com.retail_project.orderItem.OrderItemResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Integer id,
        String reference,
        BigDecimal totalAmount,
        String paymentMethod,
        Integer customerId,
        List<OrderItemResponse> items,
        LocalDateTime createdDate

) {
}
