package com.retail_project.payment;

import com.stripe.exception.EventDataObjectDeserializationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/stripe")
@RequiredArgsConstructor
public class StripeWebhookController {

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
            System.out.println("❌ Invalid Stripe signature: " + e.getMessage());
            return ResponseEntity.badRequest().body("Invalid signature");
        }

        System.out.println("📩 Received Stripe event: " + event.getType());


        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
        Object data;

        try {
            data = deserializer.deserializeUnsafe();
        } catch (EventDataObjectDeserializationException e) {
            System.out.println("❌ Deserialization error: " + e.getMessage());
            System.out.println("RAW JSON: " + deserializer.getRawJson());
            return ResponseEntity.ok("Ignored");
        }

        System.out.println("✅ Deserialized type: " + data.getClass().getSimpleName());


        switch (event.getType()) {

            // =======================================
            // A) Checkout completed → Attach intent
            // =======================================
            case "checkout.session.completed" -> {
                Session session = (Session) data;
                System.out.println("➡ checkout.session.completed");
                System.out.println("   sessionId=" + session.getId());
                System.out.println("   paymentIntentId=" + session.getPaymentIntent());
                System.out.println("   paymentStatus=" + session.getPaymentStatus());

                paymentService.attachPaymentIntentToPayment(
                        session.getId(),
                        session.getPaymentIntent()
                );

                // Handle race: payment_intent.succeeded may arrive before this event.
                // If the session is already paid, trigger completion now so nothing is missed.
                if ("paid".equals(session.getPaymentStatus())) {
                    paymentService.markPaymentAsPaidByIntent(session.getPaymentIntent());
                }
            }


            case "payment_intent.succeeded" -> {
                PaymentIntent pi = (PaymentIntent) data;

                System.out.println("➡ payment_intent.succeeded");
                System.out.println("   paymentIntentId=" + pi.getId());

                // Mark payment as paid based only on the paymentIntentId
                paymentService.markPaymentAsPaidByIntent(pi.getId());
            }


            default -> {
                System.out.println("ℹ Ignored event type: " + event.getType());
            }
        }

        return ResponseEntity.ok("Processed");
    }
}
