package com.retail_project.inventory;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory", description = "Manage product stock levels")
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping
    @Operation(summary = "Add or update inventory")
    public ResponseEntity<InventoryResponse> addOrUpdateInventory(
            @Valid @RequestBody InventoryRequest request) {
        return ResponseEntity.ok(inventoryService.addOrUpdate(request));
    }

    @GetMapping
    @Operation(summary = "Get all inventory records")
    public ResponseEntity<List<InventoryResponse>> getAllInventory() {
        return ResponseEntity.ok(inventoryService.getAll());
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "Get inventory by product ID")
    public ResponseEntity<InventoryResponse> getByProduct(@PathVariable Integer productId) {
        return ResponseEntity.ok(inventoryService.getByProductId(productId));
    }

    @DeleteMapping("/product/{productId}")
    @Operation(summary = "Delete inventory by product ID")
    public ResponseEntity<Void> deleteByProduct(@PathVariable Integer productId) {
        inventoryService.deleteByProductId(productId);
        return ResponseEntity.noContent().build();
    }
}
