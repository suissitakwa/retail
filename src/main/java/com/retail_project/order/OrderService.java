package com.retail_project.order;

import com.retail_project.cart.Cart;
import com.retail_project.cart.CartRepository;
import com.retail_project.cartItem.CartItem;
import com.retail_project.customer.Customer;
import com.retail_project.customer.CustomerRepository;
import com.retail_project.exceptions.CustomerNotFoundException;
import com.retail_project.exceptions.OrderNotFoundException;
import com.retail_project.orderItem.OrderItem;
import com.retail_project.orderItem.OrderItemRequest;
import com.retail_project.orderItem.OrderItemResponse;
import com.retail_project.payment.PaymentResponse;
import com.retail_project.payment.PaymentService;
import com.retail_project.product.Product;
import com.retail_project.product.ProductRepository;
import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
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

    @Value("${stripe.secret-key}")
    private String stripeSecretKey;

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

        return saved;
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
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

        if (!orderRepository.existsById(id)) {
            throw new OrderNotFoundException(id);
        }

        orderRepository.deleteById(id);
    }

    // ----------------------------------------------------------
    // Checkout + Stripe Session + Pending Payment
    // ----------------------------------------------------------
    @Transactional
    public PaymentResponse checkoutAndInitiatePayment(Integer customerId) throws Exception {

        // 1. Create the order from the customer's cart
        Order order = createOrderFromCart(customerId);

        // 2. Create Stripe Session
        Stripe.apiKey = stripeSecretKey;

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl("http://localhost:3001/success?orderId=" + order.getId())
                .setCancelUrl("http://localhost:3001/cancel")
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
    public Page<OrderResponse> getOrdersForCustomer(Integer customerId, Pageable pageable) {

        Page<Order> page = orderRepository.findByCustomerId(customerId, pageable);

        return page.map(order -> new OrderResponse(
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
                order.getCreatedDate()
        ));
    }
    public OrderResponse getOrderDetails(Integer orderId, int customerId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (order.getCustomer().getId()!=customerId) {
            throw new RuntimeException("Unauthorized: Not your order");
        }

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
                order.getCreatedDate()
        );
    }
    public Integer getCustomerIdByEmail(String email) {
        return customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Customer not found"))
                .getId();
    }


}
