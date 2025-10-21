package com.retail_project.product;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.retail_project.exceptions.ProductNotFoundException;
import com.retail_project.orderItem.OrderItemService;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@WebMvcTest(controllers =ProductController.class, excludeAutoConfiguration = {
    SecurityAutoConfiguration.class})
public class ProductControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;

    @Test
    void testGetProducts() throws Exception {
        ProductResponse product = new ProductResponse(1, "Laptop", "Gaming laptop", new BigDecimal("1299.99"), 1,1,"");
        when(productService.getProducts()).thenReturn(List.of(product));

        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name", is("Laptop")));
    }

    @Test
    void testGetProductById_Found() throws Exception {
        ProductResponse response = new ProductResponse(1, "Laptop", "Gaming laptop", new BigDecimal("1299.99"), 1,1,"");
        when(productService.getProductById(1)).thenReturn(response);

        mockMvc.perform(get("/api/v1/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Laptop")));
    }

    @Test
    void testGetProductById_NotFound() throws Exception {
        when(productService.getProductById(999)).thenThrow(new ProductNotFoundException(999));

        mockMvc.perform(get("/api/v1/products/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateProduct() throws Exception {
        ProductRequest request = new ProductRequest("Laptop", "Gaming laptop", new BigDecimal("1299.99"), 1,"");
        ProductResponse response = new ProductResponse(1, "Laptop", "Gaming laptop", new BigDecimal("1299.99"), 1,1,"");

        when(productService.createProduct(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Laptop")));
    }

    @Test
    void testCreateProduct_InvalidRequest() throws Exception {
        ProductRequest invalidRequest = new ProductRequest("", "", null, null,"");

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateProduct() throws Exception {
        ProductRequest request = new ProductRequest("Updated Laptop", "Updated description", new BigDecimal("1500.00"), 1,"");
        ProductResponse response = new ProductResponse(1, "Updated Laptop", "Updated description", new BigDecimal("1500.00"), 1,0,"");

        when(productService.updateProduct(eq(1), any())).thenReturn(response);

        mockMvc.perform(put("/api/v1/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated Laptop")));
    }

    @Test
    void testUpdateProduct_NotFound() throws Exception {
        ProductRequest request = new ProductRequest("Updated Laptop", "Updated description", new BigDecimal("1500.00"), 1,"");
        when(productService.updateProduct(eq(999), any())).thenThrow(new ProductNotFoundException(999));

        mockMvc.perform(put("/api/v1/products/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteProduct() throws Exception {
        doNothing().when(productService).deleteProduct(1);

        mockMvc.perform(delete("/api/v1/products/1"))
                .andExpect(status().isNoContent());

        verify(productService, times(1)).deleteProduct(1);
    }
}
