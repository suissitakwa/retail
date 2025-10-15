package com.retail_project.order;

import com.retail_project.orderItem.OrderItemRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record OrderRequest(
        @NotBlank(message = "Reference is required")
        String reference,

        @NotNull(message = "Total amount is required")
        BigDecimal totalAmount,

        @NotBlank(message = "Payment method is required")
        String paymentMethod,

        @NotNull(message = "Customer ID is required")
        Integer customerId,

        @NotEmpty(message = "At least one order item is required")
        List<OrderItemRequest> items
) {
}
