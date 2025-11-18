package com.retail_project.cart;


import com.retail_project.cartItem.CartItemRequest;
import com.retail_project.cartItem.CartItemResponse;
import com.retail_project.config.jwt.MyUserDetails;
import com.retail_project.customer.Customer;
import com.retail_project.customer.CustomerRepository;
import com.retail_project.exceptions.CustomerNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {
    private static final Logger logger = LoggerFactory.getLogger(CartController.class);
    private final CartService cartService;
    private final CustomerRepository customerRepository;

    private Customer getAuthenticatedCustomer(Authentication authentication) {
        Object principal = authentication.getPrincipal();

        if (principal instanceof MyUserDetails myUserDetails ) {
            Customer customer = myUserDetails.getCustomer();
            if (customer != null) {
                return customer;
            }
        }
            logger.error("Authentication principal is misconfigured. Expected Customer, found: {}", principal.getClass().getName());
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Authentication principal is misconfigured."
            );


    }

    @GetMapping
    public ResponseEntity<CartResponse> getCart(Authentication authentication) {
        Customer customer = getAuthenticatedCustomer(authentication);
        logger.info("Fetching cart for customer ID: {}", customer.getId());
        return ResponseEntity.ok(cartService.getOrCreateCart(customer.getId()));
    }


    @PostMapping("/add")
    public ResponseEntity<CartResponse> addItemToCart(
            @RequestBody CartItemRequest request,
            Authentication authentication) {

        Customer customer = getAuthenticatedCustomer(authentication);

        Integer customerId = customer.getId();
        logger.info("Adding item to cart for customer ID: {}", customerId);
        CartResponse response = cartService.addItemToCart(customer.getId(), request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<CartResponse> removeItem(
            @PathVariable Integer productId,
            Authentication authentication
    ) {
        if (productId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product ID cannot be null");
        }
        Customer customer = getAuthenticatedCustomer(authentication);
        logger.info("Removing product {} from cart for customer ID: {}", productId, customer.getId());
        return ResponseEntity.ok(cartService.removeItem(customer.getId(), productId));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearCart(Authentication authentication) {
        Customer customer = getAuthenticatedCustomer(authentication);
        logger.info("Clearing cart for customer ID: {}", customer.getId());
        cartService.clearCart(customer.getId());
        return ResponseEntity.noContent().build();
    }
}
