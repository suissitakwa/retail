package com.retail_project.payment;

public record CheckoutResponse(
        String redirectUrl,
        Integer orderId
) {}
