package com.retail_project.orderItem;

import com.retail_project.order.Order;
import com.retail_project.product.Product;

import java.math.BigDecimal;

public record OrderItemResponse(
        Integer id,
        Integer productId,
        String productName,
        Integer quantity,
        BigDecimal price
) {
}
