package com.retail_project.cart;

import com.retail_project.cartItem.CartItemMapper;
import com.retail_project.cartItem.CartItemResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CartMapper {
    private final CartItemMapper mapper;

    public CartResponse toResponse(Cart cart){
        List<CartItemResponse> itemResponseList = cart.getItems().stream().map(mapper::toResponse).toList();
        return new CartResponse(cart.getId(),
                cart.getCustomer().getId(),
                itemResponseList,
                cart.getCreatedAt());
    }
}
