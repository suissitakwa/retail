package com.retail_project.order;

import com.retail_project.cart.Cart;
import com.retail_project.cart.CartRepository;
import com.retail_project.cartItem.CartItem;
import com.retail_project.customer.Customer;
import com.retail_project.customer.CustomerRepository;
import com.retail_project.exceptions.CustomerNotFoundException;
import com.retail_project.exceptions.OrderNotFoundException;

import com.retail_project.order.kafka.OrderProducer;

import com.retail_project.orderItem.OrderItem;

import jakarta.transaction.Transactional;

import org.springframework.data.domain.Pageable;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final CustomerRepository customerRepository;
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
    @Transactional
    public OrderResponse checkoutCart(Integer customerId, CheckoutRequest request) {
        // 1. Find the customer's cart
        Cart cart = cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));

        if (cart.getItems().isEmpty()) {
            throw new IllegalStateException("Cannot checkout an empty cart");
        }

        // 2. Get the customer
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));

        // 3. Create new Order entity
        Order order = new Order();
        order.setCustomer(customer);
        order.setReference("ORD-" + System.currentTimeMillis()); // You may generate UUIDs instead
        try {
            order.setPaymentMethod(PaymentMethod.valueOf(request.paymentMethod()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid payment method: " + request.paymentMethod());
        }
        order.setCreatedDate(LocalDateTime.now());

        // 4. Create OrderItems from CartItems
        BigDecimal total = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem item : cart.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(item.getProduct());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setPrice(item.getPrice());

            orderItems.add(orderItem);
            total = total.add(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        order.setOrderItems(orderItems);
        order.setTotalAmount(total);

        // 5. Save order (cascade saves items)
        Order savedOrder = orderRepository.save(order);

        // 6. Clear cart
        cart.getItems().clear();
        cartRepository.save(cart);

        // 7. Return response DTO
        return orderMapper.toResponse(savedOrder);
    }

    public void deleteOrder(Integer id) {

        orderRepository.deleteById(id);
    }

}
