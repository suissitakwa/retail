package com.retail_project.inventory;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.retail_project.customer.Customer;
import com.retail_project.customer.CustomerController;
import com.retail_project.customer.CustomerService;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
@WebMvcTest(controllers = InventoryController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class})
public class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private InventoryService inventoryService;

    @Test
    void testGetInventoryByProductId() throws Exception {
        InventoryResponse response = new InventoryResponse(1, 1, "Product A", 10, LocalDateTime.now());
        when(inventoryService.getByProductId(1)).thenReturn(response);

        mockMvc.perform(get("/api/v1/inventory/product/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productName", is("Product A")));
    }

    @Test
    void testCreateOrUpdateInventory() throws Exception {
        InventoryRequest request = new InventoryRequest(1, 15);
        InventoryResponse response = new InventoryResponse(1, 1, "Product A", 15, LocalDateTime.now());

        when(inventoryService.addOrUpdate(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/inventory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity", is(15)));
    }
    @Test
    void testGetInventory_NotFound() throws Exception {
        when(inventoryService.getByProductId(999)).thenThrow(new RuntimeException("Inventory not found"));

        mockMvc.perform(get("/api/v1/inventory/product/999"))
                .andExpect(status().isNotFound());
    }
}
