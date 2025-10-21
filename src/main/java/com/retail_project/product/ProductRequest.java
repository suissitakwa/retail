package com.retail_project.product;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank(message = "Product name is required")
         String name,
        @NotBlank(message = "Description is required")
         String description,
        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Price must be positive")
         BigDecimal price,
        @NotNull(message = "Category ID is required")
        Integer categoryId,
        String imageUrl
) {
}
