package com.retail_project.payment;

import com.retail_project.order.Order;
import com.retail_project.order.OrderRepository;
import com.retail_project.order.OrderStatus;
import com.stripe.model.checkout.Session;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

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

        System.out.println("Attached paymentIntentId: " + paymentIntentId);
    }

    /**
     * Called when we receive payment_intent.succeeded.
     * Marks payment as PAID based on paymentIntentId alone.
     */
    @Transactional
    public void markPaymentAsPaidByIntent(String paymentIntentId) {

        paymentRepository.findByStripePaymentIntentId(paymentIntentId)
                .ifPresentOrElse(payment -> {
                    System.out.println("✅ Marking payment as PAID for intentId=" + paymentIntentId);

                    payment.setStatus(PaymentStatus.PAID);
                    payment.setPaidAt(LocalDateTime.now());

                    Order order = payment.getOrder();
                    order.setStatus(OrderStatus.COMPLETED);

                    orderRepository.save(order);
                    paymentRepository.save(payment);
                }, () -> {
                    System.out.println("⚠ No payment found for paymentIntentId=" + paymentIntentId);

                });
    }
}
