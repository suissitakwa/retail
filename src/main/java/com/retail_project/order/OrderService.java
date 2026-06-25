package com.retail_project.order;

import com.retail_project.Kafka.events.OrderEvent;
import com.retail_project.Kafka.events.OrderEventItem;
import com.retail_project.cart.Cart;
import com.retail_project.cart.CartRepository;
import com.retail_project.cartItem.CartItem;
import com.retail_project.customer.Customer;
import com.retail_project.customer.CustomerRepository;
import com.retail_project.exceptions.CustomerNotFoundException;
import com.retail_project.exceptions.OrderNotFoundException;
import com.retail_project.order.kafka.OrderProducer;
import com.retail_project.orderItem.OrderItem;
import com.retail_project.orderItem.OrderItemRequest;
import com.retail_project.orderItem.OrderItemResponse;
import com.retail_project.payment.PaymentResponse;
import com.retail_project.payment.PaymentService;
import com.retail_project.payment.PaymentStatus;
import com.retail_project.product.Product;
import com.retail_project.product.ProductRepository;
import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final OrderMapper orderMapper;
    private final PaymentService paymentService;
    private final ProductRepository productRepository;
    private final OrderProducer orderProducer;

    @Value("${stripe.secret-key}")
    private String stripeSecretKey;

    @Value("${app.frontend.base-url:http://localhost:3000}")
    private String frontendBaseUrl;

    // ----------------------------------------------------------------
    // Create order directly from a request (not used in checkout flow)
    // ----------------------------------------------------------------
    @Transactional
    public Order createOrder(OrderRequest request) {

        Customer customer = customerRepository.findById(request.customerId())
                .orElseThrow(() -> new CustomerNotFoundException(request.customerId()));

        Order order = new Order();
        order.setCustomer(customer);
        order.setReference(request.reference());
        order.setCreatedDate(LocalDateTime.now());
        order.setPaymentMethod(PaymentMethod.valueOf(request.paymentMethod()));
        order.setStatus(OrderStatus.PENDING);

        List<OrderItem> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemRequest itemReq : request.items()) {
            Product product = productRepository.findById(itemReq.productId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + itemReq.productId()));

            OrderItem oi = new OrderItem();
            oi.setOrder(order);
            oi.setProduct(product);
            oi.setQuantity(itemReq.quantity());
            oi.setPrice(product.getPrice());

            items.add(oi);
            total = total.add(itemReq.price().multiply(BigDecimal.valueOf(itemReq.quantity())));
        }

        order.setOrderItems(items);
        order.setTotalAmount(total);

        return orderRepository.save(order);
    }

    // ----------------------------------------------------------
    // Create Order from Cart (used by checkout)
    // ----------------------------------------------------------
    @Transactional
    public Order createOrderFromCart(Integer customerId) {

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));

        Cart cart = cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Cannot checkout an empty cart");
        }

        Order order = new Order();
        order.setCustomer(customer);
        order.setCreatedDate(LocalDateTime.now());
        order.setReference("ORD-" + System.currentTimeMillis());
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentMethod(PaymentMethod.STRIPE);

        BigDecimal total = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem item : cart.getItems()) {

            OrderItem oi = new OrderItem();
            oi.setOrder(order);
            oi.setProduct(item.getProduct());
            oi.setQuantity(item.getQuantity());
            oi.setPrice(item.getPrice());

            orderItems.add(oi);
            total = total.add(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        order.setOrderItems(orderItems);
        order.setTotalAmount(total);

        Order saved = orderRepository.save(order);

        // Clear cart after saving the order
        cart.getItems().clear();
        cartRepository.save(cart);

        // Publish order.created event for inventory deduction and notification
        List<OrderEventItem> eventItems = orderItems.stream()
                .map(oi -> new OrderEventItem(oi.getProduct().getId(), oi.getQuantity()))
                .toList();
        try {
            orderProducer.sendOrderCreatedEvent(new OrderEvent(
                    saved.getId(),
                    customer.getId(),
                    saved.getTotalAmount(),
                    eventItems
            ));
        } catch (Exception e) {
            logger.warn("Kafka unavailable — order.created event not sent for order {}: {}", saved.getId(), e.getMessage());
        }

        return saved;
    }

    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream().map(orderMapper::toResponse).toList();
    }

    public Order getOrderById(Integer id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }

    @Transactional
    public Order updateOrder(Integer id, OrderRequest request) {

        Order existing = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        existing.setReference(request.reference());
        existing.setPaymentMethod(PaymentMethod.valueOf(request.paymentMethod()));

        return orderRepository.save(existing);
    }

    @Transactional
    public void deleteOrder(Integer id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));


        order.getOrderItems().clear();
        order.getNotifications().clear();

        if (order.getPayment() != null) {
            order.setPayment(null);
        }

        orderRepository.delete(order);
    }


    // ----------------------------------------------------------
    // Checkout + Stripe Session + Pending Payment
    // ----------------------------------------------------------
    @CircuitBreaker(name = "stripe", fallbackMethod = "checkoutFallback")
    @Transactional
    public PaymentResponse checkoutAndInitiatePayment(Integer customerId) throws Exception {

        // 1. Create the order from the customer's cart
        Order order = createOrderFromCart(customerId);

        // 2. Create Stripe Session
        Stripe.apiKey = stripeSecretKey;

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(frontendBaseUrl + "/success?orderId=" + order.getId())
                .setCancelUrl(frontendBaseUrl + "/cancel")
                // Attach orderId to PaymentIntent metadata (optional, now)
                .setPaymentIntentData(
                        SessionCreateParams.PaymentIntentData.builder()
                                .putMetadata("orderId", order.getId().toString())
                                .build()
                )
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("usd")
                                                .setUnitAmount(
                                                        order.getTotalAmount()
                                                                .multiply(BigDecimal.valueOf(100))
                                                                .longValue()
                                                )
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName("Order #" + order.getId())
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                )
                .build();

        Session session = Session.create(params);

        // 3. Save PENDING payment linked to this session
        return paymentService.createPendingPayment(order, session);
    }
    public PaymentResponse checkoutFallback(Integer customerId, Throwable t) {
        throw new RuntimeException("Payment service is temporarily unavailable. Please try again in a moment.");
    }

    public Page<OrderResponse> getOrdersForCustomer(Integer customerId, Pageable pageable) {

        Page<Order> page = orderRepository.findByCustomerId(customerId, pageable);

        return page.map(order -> {
            var payment = order.getPayment();
            var paymentStatus = (payment == null) ? null : payment.getStatus();
            var paymentIntentId = (payment == null) ? null : payment.getStripePaymentIntentId();
            return new OrderResponse(
                    order.getId(),
                    order.getReference(),
                    order.getTotalAmount(),
                    order.getPaymentMethod().name(),
                    order.getCustomer().getId(),
                    order.getOrderItems().stream()
                            .map(oi -> new OrderItemResponse(
                                    oi.getId(),
                                    oi.getProduct().getId(),
                                    oi.getProduct().getName(),
                                    oi.getQuantity(),
                                    oi.getPrice()
                            )).toList(),
                    order.getCreatedDate(),
                    order.getStatus(),
                    paymentStatus,
                    paymentIntentId
            );
        });
    }
    public OrderResponse getOrderDetails(Integer orderId, int customerId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (order.getCustomer().getId()!=customerId) {
            throw new RuntimeException("Unauthorized: Not your order");
        }
        var payment = order.getPayment();
        var paymentStatus = (payment == null) ? null : payment.getStatus();
        var paymentIntentId = (payment == null) ? null : payment.getStripePaymentIntentId();

        return new OrderResponse(
                order.getId(),
                order.getReference(),
                order.getTotalAmount(),
                order.getPaymentMethod().name(),
                order.getCustomer().getId(),
                order.getOrderItems().stream().map(oi ->
                        new OrderItemResponse(
                                oi.getId(),
                                oi.getProduct().getId(),
                                oi.getProduct().getName(),
                                oi.getQuantity(),
                                oi.getPrice()
                        )
                ).toList(),
                order.getCreatedDate(),
                order.getStatus(),
                paymentStatus,
                paymentIntentId
        );
    }
    public Integer getCustomerIdByEmail(String email) {
        return customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Customer not found"))
                .getId();
    }

    @Transactional
    public void cancelOrder(Integer orderId, Integer customerId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (order.getCustomer().getId() != customerId) {
            throw new RuntimeException("Unauthorized: Not your order");
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("Only PENDING orders can be cancelled.");
        }

        var payment = order.getPayment();
        if (payment != null && payment.getStatus() == PaymentStatus.PAID) {
            paymentService.refundPayment(orderId);
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    @Transactional
    public void updateOrderStatus(Integer orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        order.setStatus(newStatus);
        orderRepository.save(order);
    }

}
