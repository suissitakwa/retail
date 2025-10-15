package com.retail_project.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.retail_project.orderItem.OrderItemRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = OrderController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class})
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    @Test
    void testGetOrders() throws Exception {
        OrderResponse order = new OrderResponse(1, "ORD123", new BigDecimal("100.00"), "CREDIT_CARD", 2,  List.of(),LocalDateTime.now());
        when(orderService.getAllOrders()).thenReturn(List.of(order));

        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].reference", is("ORD123")));
    }

    @Test
    void testCreateOrder() throws Exception {
        OrderRequest request = new OrderRequest("ORD123",new BigDecimal("100.00"), "CREDIT_CARD",2, List.of(new OrderItemRequest(1, 2)));
        OrderResponse response = new OrderResponse(1, "ORD123", new BigDecimal("100.00"), "CREDIT_CARD", 2, List.of(), LocalDateTime.now());

        when(orderService.createOrder(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reference", is("ORD123")));
    }
    @Test
    void testCreateOrder_InvalidRequest() throws Exception {
        OrderRequest request = new OrderRequest("ORDX", new BigDecimal("100.00"),"CREDIT_CARD", 1, List.of());

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
