package com.retail_project.inventory;

import jakarta.validation.constraints.Min;

public record InventoryRequest(
        Integer productId,
        @Min(value = 0, message = "Quantity must be >= 0")
        Integer quantity
) {}
