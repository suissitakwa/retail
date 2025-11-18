package com.retail_project.inventory;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface  InventoryRepository extends JpaRepository<Inventory,Integer> {
    Optional<Inventory> findByProductId(Integer productId);
}
