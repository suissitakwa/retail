package com.retail_project.order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order,Integer> {
    Order findByStripeSessionId(String sessionId);
    Page<Order> findByCustomerId(Integer customerId, Pageable pageable);

}
