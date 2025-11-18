package com.retail_project.payment;

import com.retail_project.customer.Customer;
import com.retail_project.order.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/checkout")
@RequiredArgsConstructor
public class StripeCheckoutController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<CheckoutResponse> createCheckoutSession(Authentication authentication) throws Exception {

        Customer customer = (Customer) authentication.getPrincipal();

        // OrderService handles:
        // - Cart → Order
        // - Stripe session creation (through PaymentService)
        // - Payment row creation
        PaymentResponse response = orderService.checkoutAndInitiatePayment(customer.getId());

        return ResponseEntity.ok(
                new CheckoutResponse(
                        response.redirectUrl(),
                        response.orderId()
                )
        );
    }
}
