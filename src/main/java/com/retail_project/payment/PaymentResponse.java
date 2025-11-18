package com.retail_project.payment;

public record PaymentResponse(
        String redirectUrl,
        Integer orderId
) {
}
