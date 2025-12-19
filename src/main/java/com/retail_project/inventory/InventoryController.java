package com.retail_project.inventory;

import com.retail_project.product.Product;
import com.retail_project.product.ProductRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory", description = "Manage product stock levels")
public class InventoryController {

    private final InventoryService inventoryService;
    private final ProductRepository productRepository;
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


    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/product/{productId}")
    public ResponseEntity<?> updateQty(@PathVariable Integer productId, @RequestParam Integer quantity) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (product.getInventory() == null) {
            Inventory inv = new Inventory();
            inv.setProduct(product);
            inv.setQuantity(quantity);
            product.setInventory(inv);
        } else {
            product.getInventory().setQuantity(quantity);
        }

        productRepository.save(product);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @DeleteMapping("/product/{productId}")
    @Operation(summary = "Delete inventory by product ID")
    public ResponseEntity<Void> deleteByProduct(@PathVariable Integer productId) {
        inventoryService.deleteByProductId(productId);
        return ResponseEntity.noContent().build();
    }
}
