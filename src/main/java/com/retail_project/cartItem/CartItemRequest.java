package com.retail_project.cartItem;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CartItemRequest(
        @NotNull(message = "Product ID is required")
        Integer productId,

        @NotNull
        @Min(value = 1, message = "Quantity must be at least 1")
        Integer quantity
) {}
