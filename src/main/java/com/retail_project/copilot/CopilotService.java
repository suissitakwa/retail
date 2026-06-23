package com.retail_project.copilot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openai.client.OpenAIClient;
import com.openai.core.JsonValue;
import com.openai.models.FunctionDefinition;
import com.openai.models.FunctionParameters;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.openai.models.chat.completions.ChatCompletionToolMessageParam;
import com.retail_project.order.OrderResponse;
import com.retail_project.order.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CopilotService {

    private final OrderService orderService;
    private final OpenAIClient openAIClient;

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String MODEL = "gpt-4o-mini";

    private static final FunctionParameters ORDER_ID_PARAMS = FunctionParameters.builder()
            .putAdditionalProperty("type", JsonValue.from("object"))
            .putAdditionalProperty("properties", JsonValue.from(Map.of(
                    "orderId", Map.of("type", "integer", "description", "The numeric order ID"))))
            .putAdditionalProperty("required", JsonValue.from(List.of("orderId")))
            .build();

    public CopilotResponse chat(CopilotRequest req, Authentication auth) {
        String email = auth.getName();
        Integer customerId = orderService.getCustomerIdByEmail(email);
        List<CopilotAction> actions = new ArrayList<>();

        String userMessage = req.message() != null ? req.message() : "";
        String context = req.orderId() != null
                ? "The user is referring to order ID " + req.orderId() + ". " + userMessage
                : userMessage;

        var params = ChatCompletionCreateParams.builder()
                .model(MODEL)
                .addSystemMessage("""
                        You are a retail support assistant. Rules:
                        1. Call fetch_order before answering any order-specific question.
                        2. Call cancel_order only if the user explicitly asks to cancel.
                        3. Call open_order_details or check_payment_status to suggest navigation.
                        4. Never invent order data — only use what the tools return.
                        5. Keep responses to 2-4 sentences.
                        """)
                .addUserMessage(context)
                .addFunctionTool(FunctionDefinition.builder()
                        .name("fetch_order")
                        .description("Retrieve details of a specific order. Call this whenever the user asks about an order.")
                        .parameters(ORDER_ID_PARAMS)
                        .build())
                .addFunctionTool(FunctionDefinition.builder()
                        .name("cancel_order")
                        .description("Request cancellation of an order. Only call if the user explicitly wants to cancel.")
                        .parameters(ORDER_ID_PARAMS)
                        .build())
                .addFunctionTool(FunctionDefinition.builder()
                        .name("open_order_details")
                        .description("Suggest navigating to the order details page.")
                        .parameters(ORDER_ID_PARAMS)
                        .build())
                .addFunctionTool(FunctionDefinition.builder()
                        .name("check_payment_status")
                        .description("Suggest checking the payment status for an order.")
                        .parameters(ORDER_ID_PARAMS)
                        .build())
                .build();

        try {
            var response = openAIClient.chat().completions().create(params);
            var message = response.choices().get(0).message();
            var toolCalls = message.toolCalls().orElse(List.of());

            if (toolCalls.isEmpty()) {
                return new CopilotResponse(
                        message.content().orElse("I'm here to help with your orders."),
                        actions);
            }

            // Execute tool calls and build follow-up
            var followUp = ChatCompletionCreateParams.builder()
                    .model(MODEL)
                    .addSystemMessage("You are a retail support assistant. Answer in 2-4 sentences using only the provided tool results.")
                    .addUserMessage(context)
                    .addMessage(message);

            for (var toolCall : toolCalls) {
                if (!toolCall.isFunction()) continue;
                var fn = toolCall.asFunction();
                String toolResult = dispatch(fn.function().name(), fn.function().arguments(), customerId, actions);
                followUp.addMessage(ChatCompletionToolMessageParam.builder()
                        .toolCallId(fn.id())
                        .content(toolResult)
                        .build());
            }

            var finalResponse = openAIClient.chat().completions().create(followUp.build());
            String answer = finalResponse.choices().get(0).message().content()
                    .orElse("I've looked into your order. Please see the details below.");
            return new CopilotResponse(answer, actions);

        } catch (Exception e) {
            log.error("Copilot error: {}", e.getMessage());
            return new CopilotResponse("Something went wrong. Please try again.", actions);
        }
    }

    private String dispatch(String toolName, String argsJson, Integer customerId, List<CopilotAction> actions) {
        Integer orderId = extractOrderId(argsJson);
        return switch (toolName) {
            case "fetch_order" -> fetchOrderFacts(orderId, customerId);
            case "cancel_order" -> handleCancel(orderId, customerId, actions);
            case "open_order_details" -> {
                if (orderId != null)
                    actions.add(new CopilotAction("OPEN_ORDER_DETAILS", Map.of("orderId", orderId), "View order details"));
                yield "Navigation action queued.";
            }
            case "check_payment_status" -> {
                if (orderId != null)
                    actions.add(new CopilotAction("CHECK_PAYMENTS", Map.of("orderId", orderId), "Check payment status"));
                yield "Navigation action queued.";
            }
            default -> "Unknown tool.";
        };
    }

    private Integer extractOrderId(String argsJson) {
        try {
            var node = MAPPER.readTree(argsJson);
            return node.has("orderId") ? node.get("orderId").asInt() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private String fetchOrderFacts(Integer orderId, Integer customerId) {
        if (orderId == null) return "No order ID was provided.";
        try {
            OrderResponse order = orderService.getOrderDetails(orderId, customerId);
            String items = (order.items() == null || order.items().isEmpty())
                    ? "no items"
                    : order.items().stream()
                            .map(i -> i.quantity() + " x " + i.productName())
                            .collect(Collectors.joining(", "));
            return String.format(
                    "Reference: %s. Status: %s. Total: $%.2f. Items: %s. Created: %s.",
                    order.reference(), order.status(), order.totalAmount(), items, order.createdDate());
        } catch (Exception e) {
            return "Order not found or you don't have access to it.";
        }
    }

    private String handleCancel(Integer orderId, Integer customerId, List<CopilotAction> actions) {
        if (orderId == null) return "No order ID was provided.";
        try {
            OrderResponse order = orderService.getOrderDetails(orderId, customerId);
            if ("PENDING".equalsIgnoreCase(order.status().name())) {
                actions.add(new CopilotAction("CANCEL_ORDER_CONFIRM", Map.of("orderId", orderId), "Cancel this order"));
                return "Order " + orderId + " is PENDING and eligible for cancellation.";
            } else {
                actions.add(new CopilotAction("CANCEL_NOT_ALLOWED", Map.of("orderId", orderId), "Only PENDING orders can be cancelled"));
                return "Order " + orderId + " has status " + order.status() + " and cannot be cancelled.";
            }
        } catch (Exception e) {
            return "Order not found.";
        }
    }
}
