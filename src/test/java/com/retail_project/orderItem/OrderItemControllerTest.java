package com.retail_project.orderItem;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.retail_project.orderItem.OrderItemRequest;
import com.retail_project.orderItem.OrderItemResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = OrderItemController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class})
public class OrderItemControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderItemService orderItemService;

    @Test
    void testGetByOrderId() throws Exception {
        OrderItemResponse item = new OrderItemResponse(1, 1, "Product A", 2, new BigDecimal("10.00"));
        when(orderItemService.getByOrderId(1)).thenReturn(List.of(item));

        mockMvc.perform(get("/api/v1/order-items/order/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productName", is("Product A")));
    }

    @Test
    void testUpdateOrderItem() throws Exception {
        OrderItemRequest request = new OrderItemRequest(1, 5,BigDecimal.ZERO);
        OrderItemResponse response = new OrderItemResponse(1, 1, "Product A", 5, new BigDecimal("10.00"));

        when(orderItemService.update(eq(1), any())).thenReturn(response);

        mockMvc.perform(put("/api/v1/order-items/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity", is(5)));
    }
    @Test
    void testUpdateOrderItem_NotFound() throws Exception {
        OrderItemRequest request = new OrderItemRequest(1, 5,BigDecimal.ZERO);

        when(orderItemService.update(eq(999), any())).thenThrow(new RuntimeException("OrderItem not found"));

        mockMvc.perform(put("/api/v1/order-items/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
}
