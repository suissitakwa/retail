package com.retail_project.cart;


import com.retail_project.cartItem.CartItem;
import com.retail_project.cartItem.CartItemMapper;
import com.retail_project.cartItem.CartItemRepository;
import com.retail_project.cartItem.CartItemRequest;
import com.retail_project.customer.Customer;
import com.retail_project.customer.CustomerRepository;
import com.retail_project.product.Product;
import com.retail_project.product.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final CartMapper cartMapper;
    private final CartItemMapper cartItemMapper;

    // Create or return existing cart
    @Transactional
    public CartResponse getOrCreateCart(Integer customerId) {
        Cart cart = cartRepository.findByCustomerId(customerId)
                .orElseGet(() -> {
                    Customer customer = customerRepository.findById(customerId)
                            .orElseThrow(() -> new RuntimeException("Customer not found"));
                    Cart newCart = new Cart();
                    newCart.setCustomer(customer);
                    return cartRepository.save(newCart);
                });

        return cartMapper.toResponse(cart);
    }


    @Transactional
    public CartResponse addItemToCart(Integer customerId, CartItemRequest request) {
        Cart cart = cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Optional<CartItem> existingItem = cart.getItems()
                .stream()
                .filter(item -> item.getProduct().getId().equals(request.productId()))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + request.quantity());
        } else {
            CartItem newItem = cartItemMapper.toEntity(request, product, cart);
            cart.getItems().add(newItem);
        }

        return cartMapper.toResponse(cartRepository.save(cart));
    }

    // Remove item from cart
    @Transactional
    public CartResponse removeItem(Integer customerId, Integer productId) {
        Cart cart = cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        cart.getItems().removeIf(item -> item.getProduct().getId().equals(productId));

        return cartMapper.toResponse(cartRepository.save(cart));
    }

    // Clear all items
    @Transactional
    public void clearCart(Integer customerId) {
        Cart cart = cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        cart.getItems().clear();
        cartRepository.save(cart);
    }
}
