package com.retail_project.order;

import com.retail_project.orderItem.OrderItemResponse;
import com.retail_project.payment.PaymentStatus;

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
        LocalDateTime createdDate,


        OrderStatus status,
        PaymentStatus paymentStatus,
        String stripePaymentIntentId


) {
}
