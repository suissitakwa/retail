package com.retail_project.order;

import com.retail_project.exceptions.OrderNotFoundException;

import com.retail_project.order.kafka.OrderProducer;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;



import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderProducer orderProducer;
    private final OrderMapper orderMapper;
    private final OrderFactory orderFactory;

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        Order order = orderFactory.createFromRequest(request);
        Order saved = orderRepository.save(order);
        //orderProducer.sendOrderCreatedEvent(new OrderEvent(saved.getId(), saved.getTotalAmount()));
        return orderMapper.toResponse(saved);
    }





    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream().map(orderMapper::toResponse).toList();
    }

    public OrderResponse getOrderById(Integer id) {
        Order order =orderRepository.findById(id).orElseThrow(()->new OrderNotFoundException(id));
        return orderMapper.toResponse(order);
    }
    public Page<OrderResponse> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable)
                .map(orderMapper::toResponse);
    }

    public OrderResponse updateOrder(Integer id, OrderRequest request) {
        Order existing = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        existing.setReference(request.reference());
        existing.setPaymentMethod(PaymentMethod.valueOf(request.paymentMethod()));
        return orderMapper.toResponse(orderRepository.save(existing));
    }

    public void deleteOrder(Integer id) {

        orderRepository.deleteById(id);
    }

}
