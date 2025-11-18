package com.retail_project.orderItem;

import org.springframework.stereotype.Component;

@Component
public class OrderItemMapper {
    public OrderItemResponse toResponse(OrderItem item) {
        return new OrderItemResponse(
                item.getId(),
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getQuantity(),
                item.getPrice()
        );
    }
}
