package com.retail_project.product;

import java.math.BigDecimal;

public record ProductResponse(
        Integer id,
        String name,
        String description,
        BigDecimal price,
        Integer categoryId,
        double availableQuantity,
        String imageUrl
) {
}
