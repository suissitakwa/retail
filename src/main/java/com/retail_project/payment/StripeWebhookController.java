package com.retail_project.payment;

import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.net.RequestOptions;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/stripe")
@RequiredArgsConstructor
public class StripeWebhookController {
/*
    private final PaymentService paymentService;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signature) {

        Event event;

        try {
            event = Webhook.constructEvent(payload, signature, webhookSecret);

        } catch (Exception e) {
            System.out.println("❌ Signature verification error: " + e.getMessage());
            return ResponseEntity.badRequest().body("Invalid signature");
        }

        System.out.println("📩 Stripe Event: " + event.getType());


        RequestOptions options = RequestOptions.builder()
                //.setApiVersionOverride("2024-06-20")
                .build();

        EventDataObjectDeserializer deserializer =
                event.getDataObjectDeserializer();

        if (deserializer.getObject().isEmpty()) {
            System.out.println("❌ Deserialization failed, raw JSON:");
            System.out.println(deserializer.getRawJson());
            return ResponseEntity.ok("Ignored");
        }

        Object data = deserializer.deserializeUnsafe();
        System.out.println("✅ Deserialized class: " + data.getClass().getName());

        switch (event.getType()) {

            case "checkout.session.completed" -> {
                Session session = (Session) data;
                System.out.println("➡ checkout.session.completed sessionId=" + session.getId());
                System.out.println("➡ paymentIntentId=" + session.getPaymentIntent());

                paymentService.attachPaymentIntentToPayment(
                        session.getId(),
                        session.getPaymentIntent()
                );
            }

            case "payment_intent.succeeded" -> {
                PaymentIntent pi = (PaymentIntent) data;
                System.out.println("➡ payment_intent.succeeded intentId=" + pi.getId());
                paymentService.markPaymentAsPaidByIntent(pi.getId());
            }
        }

        return ResponseEntity.ok("Processed");


    }
     */

}
