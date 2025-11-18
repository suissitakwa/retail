package com.retail_project.inventory;

import com.retail_project.product.Product;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class InventoryMapper {
    public Inventory toEntity(InventoryRequest request, Product product) {
        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setQuantity(request.quantity());
        inventory.setLastUpdated(LocalDateTime.now());
        return inventory;
    }

    public InventoryResponse toResponse(Inventory inventory) {
        return new InventoryResponse(
                inventory.getId(),
                inventory.getProduct().getId(),
                inventory.getProduct().getName(),
                inventory.getQuantity(),
                inventory.getLastUpdated()
        );
    }
}
