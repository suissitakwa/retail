package com.retail_project.cart;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface  CartRepository extends JpaRepository<Cart,Integer> {
Optional<Cart> findByCustomerId(Integer CustomerId);
}
