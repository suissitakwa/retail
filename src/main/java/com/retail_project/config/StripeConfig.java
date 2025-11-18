package com.retail_project.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StripeConfig {

    @Value("${stripe.secret-key}")
    private String secretKey;

   // @Value("${stripe.api-version:2024-06-20}")
   // private String apiVersion;

    @PostConstruct
    public void init() {
        Stripe.apiKey = secretKey;


        System.out.println("Stripe initialized.");
    }
}
