package com.retail_project.payment;

import com.retail_project.order.Order;
import com.retail_project.order.OrderRepository;
import com.retail_project.order.OrderStatus;
import com.stripe.model.checkout.Session;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

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



    public void markPaymentAsPaidByIntent(String paymentIntentId) {

        Payment payment = paymentRepository
                .findByStripePaymentIntentId(paymentIntentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        payment.setStatus(PaymentStatus.PAID);
        payment.setPaidAt(LocalDateTime.now());

        Order order = payment.getOrder();
        order.setStatus(OrderStatus.COMPLETED);

        orderRepository.save(order);
        paymentRepository.save(payment);
    }
    public void attachPaymentIntentToPayment(String sessionId, String paymentIntentId) {

        Payment payment = paymentRepository.findByStripeSessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        payment.setStripePaymentIntentId(paymentIntentId);
        paymentRepository.save(payment);

        System.out.println("Attached paymentIntentId: " + paymentIntentId);
    }
}
