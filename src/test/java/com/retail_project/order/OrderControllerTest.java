package com.retail_project.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = OrderController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class
)
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // NEW Spring Boot 3.4+ annotation
    @MockitoBean
    private OrderService orderService;

    // --------------------------------------------------------------
    // TEST: GET all orders
    // --------------------------------------------------------------
    /*
    @Test
    void testGetOrders() throws Exception {
        Order order = new Order();
        order.setId(1);
        order.setReference("ORD123");
        order.setTotalAmount(new BigDecimal("100.00"));
        order.setCreatedDate(LocalDateTime.now());

        when(orderService.getAllOrders()).thenReturn(List.of(order));

        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].reference", is("ORD123")));
    }
*/
    // --------------------------------------------------------------
    // TEST: Create Order
    // --------------------------------------------------------------
    @Test
    void testCreateOrder() throws Exception {

        OrderRequest request = new OrderRequest(
                "ORD123",
                new BigDecimal("100.00"),
                "CREDIT_CARD",
                2,
                List.of()     // items omitted for simplicity
        );

        Order saved = new Order();
        saved.setId(1);
        saved.setReference("ORD123");
        saved.setTotalAmount(new BigDecimal("100.00"));
        saved.setCreatedDate(LocalDateTime.now());

        when(orderService.createOrder(any())).thenReturn(saved);

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reference", is("ORD123")));
    }

    // --------------------------------------------------------------
    // TEST: Create Invalid Order (Example: wrong payload)
    // --------------------------------------------------------------
    @Test
    void testCreateOrder_InvalidRequest() throws Exception {

        // This request will fail inside your validation/business rules
        OrderRequest invalid = new OrderRequest(
                "INVALID",
                new BigDecimal("100.00"),
                "CREDIT_CARD",
                1,
                List.of()
        );

        // No stubbing needed — controller will call service -> service may throw
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }
}
