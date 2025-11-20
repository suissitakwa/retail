package com.retail_project.order;

import com.retail_project.customer.Customer;
import com.retail_project.payment.CheckoutResponse;
import com.retail_project.payment.PaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
    @GetMapping("/my-orders")
    public ResponseEntity<Page<OrderResponse>> getMyOrders(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        String email = authentication.getName();
        System.out.println("inside getMyOrders checkoutAndInitiatePayment this is my email: "+email);
        Integer customerId = orderService.getCustomerIdByEmail(email);

        Page<OrderResponse> orders = orderService.getOrdersForCustomer(
                customerId,
                PageRequest.of(page, size, Sort.by("createdDate").descending())
        );

        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderDetails(
            @PathVariable Integer id,
            Authentication authentication) {

        String email = authentication.getName();
        Integer customerId = orderService.getCustomerIdByEmail(email);


        OrderResponse order = orderService.getOrderDetails(id, customerId);

        return ResponseEntity.ok(order);
    }


    // --------------------------------------------
    // Get all orders
    // --------------------------------------------
    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
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
