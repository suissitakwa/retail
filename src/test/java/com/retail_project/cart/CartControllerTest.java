package com.retail_project.cart;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.retail_project.cartItem.CartItemRequest;
import com.retail_project.config.jwt.MyUserDetails;
import com.retail_project.customer.Customer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = CartController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class
)
class CartControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean
    private CartService cartService;
    @MockitoBean private Authentication authentication;

    // Utility: mock authentication → returns a Customer object
    private void mockAuthWithCustomer(int customerId) {
        Customer customer = new Customer();
        customer.setId(customerId);

        MyUserDetails userDetails = new MyUserDetails(customer);

        when(authentication.getPrincipal()).thenReturn(userDetails);
    }

    @Test
    void testGetCart() throws Exception {
        mockAuthWithCustomer(1);

        var response = new CartResponse(1, 1, List.of(), BigDecimal.ZERO,
                LocalDateTime.now());
        when(cartService.getOrCreateCart(1)).thenReturn(response);

        mockMvc.perform(get("/api/v1/cart")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value(1));
    }

    @Test
    void testAddItemToCart() throws Exception {
        mockAuthWithCustomer(1);

        CartItemRequest request = new CartItemRequest(1, 2);
        var response = new CartResponse(1, 1, List.of(), BigDecimal.ZERO,
                LocalDateTime.now());

        when(cartService.addItemToCart(eq(1), any(CartItemRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/cart/add")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void testRemoveItem() throws Exception {
        mockAuthWithCustomer(1);

        var response = new CartResponse(1, 1, List.of(), BigDecimal.ZERO,
                LocalDateTime.now());
        when(cartService.removeItem(1, 2)).thenReturn(response);

        mockMvc.perform(delete("/api/v1/cart/remove/2")
                        .principal(authentication))
                .andExpect(status().isOk());
    }

    @Test
    void testClearCart() throws Exception {
        mockAuthWithCustomer(1);

        doNothing().when(cartService).clearCart(1);

        mockMvc.perform(delete("/api/v1/cart/clear")
                        .principal(authentication))
                .andExpect(status().isNoContent());
    }
}
