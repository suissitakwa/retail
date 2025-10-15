package com.retail_project.order;

import com.retail_project.orderItem.OrderItemMapper;
import com.retail_project.orderItem.OrderItemResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
public class OrderMapper {
    private final OrderItemMapper orderItemMapper;


    public OrderResponse toResponse(Order order){
        List<OrderItemResponse> itemResponses = order.getOrderItems().stream()
                .map(orderItemMapper::toResponse)
                .toList();
        return new OrderResponse(order.getId(),
                order.getReference(),
                order.getTotalAmount(),
                order.getPaymentMethod().name(),
                order.getCustomer().getId(),
                itemResponses,
                order.getCreatedDate()
        );
    }
}
