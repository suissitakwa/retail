package com.retail_project.cartItem;

import com.retail_project.cart.Cart;
import com.retail_project.product.Product;
import org.springframework.stereotype.Component;

@Component
public class CartItemMapper {
    public CartItem toEntity(CartItemRequest request, Product product, Cart cart) {
        CartItem item = new CartItem();
        item.setProduct(product);
        item.setCart(cart);
        item.setQuantity(request.quantity());
        item.setPrice(product.getPrice());
        return item;
    }
    public CartItemResponse toResponse(CartItem cartItem){
        return new CartItemResponse(
            cartItem.getProduct().getId(),
            cartItem.getProduct().getName(),
            cartItem.getQuantity(),cartItem.getProduct().getPrice() );
    }
}
