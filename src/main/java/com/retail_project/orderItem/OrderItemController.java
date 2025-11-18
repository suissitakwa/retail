package com.retail_project.orderItem;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/order-items")
@RequiredArgsConstructor
@Tag(name = "Order Items", description = "Manage individual order items")
public class OrderItemController {

    private final OrderItemService orderItemService;

    @GetMapping
    @Operation(summary = "Get all order items")
    public ResponseEntity<List<OrderItemResponse>> getAll() {
        return ResponseEntity.ok(orderItemService.getAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order item by ID")
    public ResponseEntity<OrderItemResponse> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(orderItemService.getById(id));
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get order items by order ID")
    public ResponseEntity<List<OrderItemResponse>> getByOrderId(@PathVariable Integer orderId) {
        return ResponseEntity.ok(orderItemService.getByOrderId(orderId));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an order item")
    public ResponseEntity<OrderItemResponse> update(
            @PathVariable Integer id,
            @Valid @RequestBody OrderItemRequest request) {
        return ResponseEntity.ok(orderItemService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an order item")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        orderItemService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

