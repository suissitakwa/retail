package com.retail_project.cartItem;

import java.math.BigDecimal;

public record CartItemResponse(
        Integer productId,
        String productName,
        Integer quantity,
        BigDecimal price
) {
}
