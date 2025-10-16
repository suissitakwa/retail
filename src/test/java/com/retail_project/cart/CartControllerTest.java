package com.retail_project.cart;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.retail_project.cartItem.CartItemRequest;
import com.retail_project.product.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CartController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class})
class CartControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private CartService cartService;

    @Test
    void testGetCart() throws Exception {
        var response = new CartResponse(1, 1, List.of(), null);

        when(cartService.getOrCreateCart(1)).thenReturn(response);

        mockMvc.perform(get("/api/v1/cart/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value(1));
    }

    @Test
    void testAddItemToCart() throws Exception {
        CartItemRequest request = new CartItemRequest(1, 2);
        var cartResponse = new CartResponse(1, 1, List.of(), null);

        when(cartService.addItemToCart(eq(1), any(CartItemRequest.class)))
                .thenReturn(cartResponse);

        mockMvc.perform(post("/api/v1/cart/1/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void testRemoveItem() throws Exception {
        var cartResponse = new CartResponse(1, 1, List.of(), null);

        when(cartService.removeItem(1, 2)).thenReturn(cartResponse);

        mockMvc.perform(delete("/api/v1/cart/1/remove/2"))
                .andExpect(status().isOk());
    }

    @Test
    void testClearCart() throws Exception {
        mockMvc.perform(delete("/api/v1/cart/1/clear"))
                .andExpect(status().isNoContent());
    }
}

