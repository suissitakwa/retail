package com.retail_project.inventory;

import com.retail_project.exceptions.ProductNotFoundException;
import com.retail_project.product.Product;
import com.retail_project.product.ProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@Service
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final InventoryMapper mapper;
    public InventoryResponse addOrUpdate(InventoryRequest request) {
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ProductNotFoundException(request.productId()));

        Inventory inventory = inventoryRepository.findByProductId(product.getId())
                .orElse(new Inventory());

        inventory.setProduct(product);
        inventory.setQuantity(request.quantity());
        inventory.setLastUpdated(LocalDateTime.now());

        return mapper.toResponse(inventoryRepository.save(inventory));
    }
    public InventoryResponse getByProductId(Integer productId) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Inventory not found for product ID: " + productId));
        return mapper.toResponse(inventory);
    }
    public List<InventoryResponse> getAll() {
        return inventoryRepository.findAll()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }
    public void deleteByProductId(Integer productId) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Inventory not found for product ID: " + productId));
        inventoryRepository.delete(inventory);
    }
}
