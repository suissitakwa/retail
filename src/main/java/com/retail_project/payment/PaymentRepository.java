package com.retail_project.payment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment,Integer> {
    Optional<Payment> findByStripeSessionId(String sessionId);
    Optional<Payment> findByStripePaymentIntentId(String stripePaymentIntentId);

}
