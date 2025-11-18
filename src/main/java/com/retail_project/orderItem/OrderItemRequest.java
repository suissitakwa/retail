package com.retail_project.orderItem;

import com.retail_project.order.Order;
import com.retail_project.product.Product;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record OrderItemRequest(
        @NotNull(message = "Product ID is required")
        Integer productId,

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        Integer quantity,
        BigDecimal price
) {
}
