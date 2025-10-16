package com.retail_project.cart;

import com.retail_project.cartItem.CartItemResponse;

import java.time.LocalDateTime;
import java.util.List;

public record CartResponse(
        Integer id,
        Integer customerId,
        List<CartItemResponse> items,
        LocalDateTime createdAt
) {
}
