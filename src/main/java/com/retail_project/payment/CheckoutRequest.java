package com.retail_project.payment;

import jakarta.validation.constraints.NotNull;

public record CheckoutRequest(
        @NotNull(message = "Payment method is required")
        String paymentMethod
) {}
