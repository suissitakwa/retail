package com.retail_project.cart;


import com.retail_project.cartItem.CartItemRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping("/{customerId}")
    public ResponseEntity<CartResponse> getCart(@PathVariable Integer customerId) {
        return ResponseEntity.ok(cartService.getOrCreateCart(customerId));
    }
    @PostMapping("/add")
    public ResponseEntity<CartResponse> addItemWithoutCustomer(
            @RequestBody @Valid CartItemRequest request
    ) {
        Integer customerId = 1; // or extract from JWT/session later
        return ResponseEntity.ok(cartService.addItemToCart(customerId, request));
    }

    @PostMapping("/{customerId}/add")
    public ResponseEntity<CartResponse> addItem(
            @PathVariable Integer customerId,
            @RequestBody @Valid CartItemRequest request
    ) {
        return ResponseEntity.ok(cartService.addItemToCart(customerId, request));
    }

    @DeleteMapping("/{customerId}/remove/{productId}")
    public ResponseEntity<CartResponse> removeItem(
            @PathVariable Integer customerId,
            @PathVariable Integer productId
    ) {
        return ResponseEntity.ok(cartService.removeItem(customerId, productId));
    }

    @DeleteMapping("/{customerId}/clear")
    public ResponseEntity<Void> clearCart(@PathVariable Integer customerId) {
        cartService.clearCart(customerId);
        return ResponseEntity.noContent().build();
    }
}
