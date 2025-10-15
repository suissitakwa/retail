package com.retail_project.order;

import com.retail_project.orderItem.OrderItemRequest;

import java.math.BigDecimal;
import java.util.List;

public record OrderRequest(
        String reference,
        BigDecimal totalAmount,
        String paymentMethod,
        Integer customerId,
        List<OrderItemRequest> items
) {
}
