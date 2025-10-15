package com.retail_project.orderItem;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository <OrderItem,Integer>{
    List<OrderItem> findByOrderId(Integer orderId);
}
