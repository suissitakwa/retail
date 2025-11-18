package com.retail_project.cart;

import com.retail_project.cartItem.CartItemMapper;
import com.retail_project.cartItem.CartItemResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CartMapper {
    private final CartItemMapper mapper;

    public CartResponse toResponse(Cart cart){
        BigDecimal total = cart.getItems().stream()
                .map(item->item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO,BigDecimal::add);
        List<CartItemResponse> itemResponseList = cart.getItems().stream().map(mapper::toResponse).toList();
        return new CartResponse(cart.getId(),
                cart.getCustomer().getId(),
                itemResponseList,
               total,
                cart.getCreatedAt());
    }
}
