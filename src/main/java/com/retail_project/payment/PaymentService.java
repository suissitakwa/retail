package com.retail_project.payment;

import com.retail_project.Kafka.events.PaymentEvent;
import com.retail_project.email.EmailService;
import com.retail_project.order.Order;
import com.retail_project.order.OrderRepository;
import com.retail_project.order.OrderStatus;
import com.retail_project.orderItem.OrderItem;
import com.retail_project.payment.kafka.PaymentProducer;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Refund;
import com.stripe.model.checkout.Session;
import com.stripe.param.RefundCreateParams;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PaymentProducer paymentProducer;
    private final EmailService emailService;

    @Value("${stripe.secret-key}")
    private String stripeSecretKey;

    /**
     * Called right after creating the Stripe Session.
     * Creates a PENDING payment row linked to the Order + sessionId.
     */
    @Transactional
    public PaymentResponse createPendingPayment(Order order, Session session) {

        Payment payment = Payment.builder()
                .order(order)
                .stripeSessionId(session.getId())
                .status(PaymentStatus.PENDING)
                .amount(order.getTotalAmount())
                .build();

        paymentRepository.save(payment);

        return new PaymentResponse(
                session.getUrl(),
                order.getId()
        );
    }

    /**
     * Called when we receive checkout.session.completed.
     * Attach the PaymentIntent ID to the existing Payment row via sessionId.
     */
    @Transactional
    public void attachPaymentIntentToPayment(String sessionId, String paymentIntentId) {

        Payment payment = paymentRepository.findByStripeSessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("Payment not found for sessionId=" + sessionId));

        payment.setStripePaymentIntentId(paymentIntentId);
        paymentRepository.save(payment);

        log.debug("Attached paymentIntentId {} to session {}", paymentIntentId, sessionId);
    }

    /**
     * Called when we receive payment_intent.succeeded.
     * Marks payment as PAID based on paymentIntentId alone.
     */
    @Transactional
    public void markPaymentAsPaidByIntent(String paymentIntentId) {

        paymentRepository.findByStripePaymentIntentId(paymentIntentId)
                .ifPresentOrElse(payment -> {
                    if (payment.getStatus() == PaymentStatus.PAID) {
                        log.debug("Payment already PAID for intentId={}, skipping", paymentIntentId);
                        return;
                    }
                    log.info("Marking payment as PAID for intentId={}", paymentIntentId);

                    payment.setStatus(PaymentStatus.PAID);
                    payment.setPaidAt(LocalDateTime.now());

                    Order order = payment.getOrder();
                    order.setStatus(OrderStatus.COMPLETED);

                    orderRepository.save(order);
                    paymentRepository.save(payment);

                    try {
                        var customer = order.getCustomer();
                        List<String> itemLines = order.getOrderItems().stream()
                                .map(i -> i.getQuantity() + " × " + i.getProduct().getName())
                                .toList();
                        emailService.sendOrderConfirmation(
                                customer.getEmail(),
                                customer.getFirstname(),
                                order.getReference(),
                                payment.getAmount(),
                                itemLines);
                    } catch (Exception e) {
                        log.warn("Failed to send order confirmation email: {}", e.getMessage());
                    }

                    try {
                        paymentProducer.sendPaymentProcessedEvent(new PaymentEvent(
                                order.getId(),
                                order.getCustomer().getId(),
                                paymentIntentId,
                                "PAID",
                                payment.getAmount()
                        ));
                    } catch (Exception e) {
                        // Kafka unavailable — payment marked PAID, notification skipped
                    }
                }, () -> log.warn("No payment found for paymentIntentId={}", paymentIntentId));
    }

    @Transactional
    public void refundPayment(Integer orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found for orderId=" + orderId));

        if (payment.getStatus() != PaymentStatus.PAID) {
            throw new RuntimeException("Only PAID payments can be refunded");
        }

        try {
            Stripe.apiKey = stripeSecretKey;
            Refund.create(RefundCreateParams.builder()
                    .setPaymentIntent(payment.getStripePaymentIntentId())
                    .build());
        } catch (StripeException e) {
            throw new RuntimeException("Stripe refund failed: " + e.getMessage());
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        paymentRepository.save(payment);
    }
}
