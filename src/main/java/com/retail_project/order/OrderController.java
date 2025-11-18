package com.retail_project.order;

import com.retail_project.payment.CheckoutResponse;
import com.retail_project.payment.PaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // --------------------------------------------
    // Create Manual Order (Admin / Non-cart)
    // --------------------------------------------
    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody OrderRequest request) {
        return ResponseEntity.ok(orderService.createOrder(request));
    }

    // --------------------------------------------
    // Checkout current cart → Stripe Checkout
    // --------------------------------------------
    @PostMapping("/checkout")
    public ResponseEntity<CheckoutResponse> checkoutCart(@RequestParam Integer customerId) throws Exception {

        PaymentResponse response = orderService.checkoutAndInitiatePayment(customerId);

        return ResponseEntity.ok(
                new CheckoutResponse(
                        response.redirectUrl(),
                        response.orderId()
                )
        );
    }


    // --------------------------------------------
    // Get all orders
    // --------------------------------------------
    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    // --------------------------------------------
    // Get single order
    // --------------------------------------------
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Integer id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    // --------------------------------------------
    // Update order
    // --------------------------------------------
    @PutMapping("/{id}")
    public ResponseEntity<Order> updateOrder(
            @PathVariable Integer id,
            @RequestBody OrderRequest request
    ) {
        return ResponseEntity.ok(orderService.updateOrder(id, request));
    }

    // --------------------------------------------
    // Delete order
    // --------------------------------------------
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Integer id) {
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }
}
