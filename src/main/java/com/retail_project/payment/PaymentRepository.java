package com.retail_project.payment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {

    Optional<Payment> findByStripeSessionId(String stripeSessionId);

    Optional<Payment> findByStripePaymentIntentId(String paymentIntentId);
}

