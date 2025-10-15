package com.retail_project.inventory;

import java.time.LocalDateTime;

public record InventoryResponse(
        Integer id,
        Integer productId,
        String productName,
        Integer quantity,
        LocalDateTime lastUpdated
) {
}
