package com.retail_project.copilot;

import com.retail_project.exceptions.OrderNotFoundException;
import com.retail_project.order.OrderResponse;
import com.retail_project.order.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import com.openai.client.OpenAIClient;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CopilotService {

    private final OrderService orderService;
    private final OpenAIClient openAIClient;
    private final String model = System.getProperty("OPENAI_MODEL", "gpt-4o-mini");

    public CopilotResponse chat(CopilotRequest req, Authentication auth) {
        List<CopilotAction> actions = new ArrayList<>();
        String msg = (req.message() == null) ? "" : req.message().toLowerCase();
        Integer orderId = req.orderId();

        String email = auth.getName();
        Integer customerId = orderService.getCustomerIdByEmail(email);


        boolean talkingAboutOrders = msg.contains("order") || msg.contains("delayed") || msg.contains("shipping");
        boolean talkingAboutPayment = msg.contains("payment") || msg.contains("stripe") || msg.contains("charged");
        boolean talkingAboutCancel = msg.contains("cancel");

        if ((talkingAboutOrders || talkingAboutPayment || talkingAboutCancel) && orderId == null) {
            return new CopilotResponse("Sure — please provide your orderId (example: 752).", actions);
        }


        if (orderId != null) {
            try {
                OrderResponse order = orderService.getOrderDetails(orderId, customerId);


                if (talkingAboutCancel) {
                    if ("PENDING".equalsIgnoreCase(order.status().name())) {
                        actions.add(new CopilotAction("CANCEL_ORDER_CONFIRM", Map.of("orderId", orderId), "Cancel this order"));
                    } else {
                        actions.add(new CopilotAction("CANCEL_NOT_ALLOWED", Map.of("orderId", orderId), "Only PENDING orders can be cancelled"));
                    }
                }

                String itemsText = (order.items() == null || order.items().isEmpty())
                        ? "no items"
                        : order.items().stream()
                        .map(i -> i.quantity() + " x " + i.productName())
                        .collect(Collectors.joining(", "));

                String facts = String.format(
                        "Order reference: %s. Status: %s. Total: $%.2f. Items: %s. Created: %s.",
                        order.reference(),
                        order.status(),
                        order.totalAmount(),
                        itemsText,
                        order.createdDate().toString()
                );

                actions.add(new CopilotAction("OPEN_ORDER_DETAILS", Map.of("orderId", orderId), "View order details"));
                actions.add(new CopilotAction("CHECK_PAYMENTS", Map.of("orderId", orderId), "Check payment status"));

                String answer = polishWithOpenAI(req.message(), facts);
                return new CopilotResponse(answer, actions);

            } catch (OrderNotFoundException e) {
                return new CopilotResponse("I couldn’t find that order for your account.", actions);
            }
            catch (RuntimeException e) {
                return new CopilotResponse("I couldn’t access that order for your account.", actions);
            }
            catch (Exception e) {
                return new CopilotResponse("Something went wrong while reading your order. Please try again.", actions);
            }
        }

        return new CopilotResponse("I can help with orders and payments. Try: 'Where is my order 752?'", actions);
    }

    private String polishWithOpenAI(String userMessage, String facts) {
        String prompt = String.format("""
            You are a retail support assistant. 
            Rules:
            1) Use ONLY these facts: %s
            2) Don't invent status. Include reference and total.
            3) Keep it to 2-4 sentences.
            User message: %s
            """, facts, userMessage);

        var params = ChatCompletionCreateParams.builder()
                .model(model)
                .addSystemMessage("You are a retail support assistant. Use only provided facts. Do not invent.")
                .addUserMessage(prompt)
                .build();


        var response = openAIClient.chat().completions().create(params);
        return response.choices().get(0).message().content().orElse("I can see your order, but I'm having trouble phrasing a response.");
    }
}