package com.retail_project.copilot;

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

        if ((talkingAboutOrders || talkingAboutPayment) && orderId == null) {
            return new CopilotResponse("Sure — please provide your orderId (example: 752).", actions);
        }

        if (orderId != null) {
            try {
                OrderResponse order = orderService.getOrderDetails(orderId, customerId);

                // Fixed the stream logic
                String itemsText = (order.items() == null || order.items().isEmpty())
                        ? "no items"
                        : order.items().stream()
                        .map(i -> i.quantity()+ " x " + i.productName())
                        .collect(Collectors.joining(", "));

                String facts = String.format(
                        "Order reference: %s. Total: $%.2f. Items: %s. Created: %s.",
                        order.reference(),
                        order.totalAmount(),
                        itemsText,
                        order.createdDate().toString()
                );
                actions.add(new CopilotAction(
                        "OPEN_ORDER_DETAILS",
                        Map.of("orderId", orderId),
                        "View order details"
                ));

                actions.add(new CopilotAction(
                        "CHECK_PAYMENTS",
                        Map.of("orderId", orderId),
                        "Check payment status"
                ));
                String answer = polishWithOpenAI(req.message(), facts);
                return new CopilotResponse(answer, actions);

            } catch (Exception e) {
                return new CopilotResponse(
                        "I couldn’t find that order for your account. Please double-check the orderId.",
                        actions
                );
            }
        }

        return new CopilotResponse(
                "I can help with orders and payments. Ask “Where is my order?” and include orderId=752.",
                actions
        );
    }

    private String polishWithOpenAI(String userMessage, String facts) {
        String prompt = """
        You are a retail support assistant.
        Rules:
        1) Use ONLY these facts: %s
        2) Don't invent status. Include reference and total.
        3) 2-4 sentences.
        User message: %s
        """.formatted(facts, userMessage);

        var params = ChatCompletionCreateParams.builder()
                .model(model)
                .addUserMessage(prompt)
                .build();

        return openAIClient.chat().completions().create(params)
                .choices().get(0).message().content()
                .orElse("I'm sorry, I'm having trouble with my AI brain right now."); // Fixed Optional
    }
}