package com.retail_project.customer;


import com.fasterxml.jackson.databind.ObjectMapper;

import com.retail_project.customer.Customer;
import com.retail_project.customer.CustomerController;
import com.retail_project.customer.CustomerService;
import com.retail_project.exceptions.CustomerNotFoundException;
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

import java.util.List;
import java.util.Optional;

@WebMvcTest(controllers = CustomerController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration.class
              //  JpaAuditingAutoConfiguration.class
} )
public class CustomerControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private  CustomerService customerService;




    @Test
    void testGetCustomers() throws Exception {
        CustomerResponse customer = new CustomerResponse(1, "John", "Doe", "john@gmail.com", "1295 Montague Expressway", Role.ROLE_ADMIN);
        when(customerService.getCustomers()).thenReturn(List.of(customer));

        mockMvc.perform(get("/api/v1/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstname", is("John")));
    }

    @Test
    void testGetCustomerById_Found() throws Exception {
        CustomerResponse customer = new CustomerResponse(1, "John", "Doe", "john@gmail.com", "1295 Montague Expressway", Role.ROLE_ADMIN);
        when(customerService.getCustomerById(1)).thenReturn(customer);

        mockMvc.perform(get("/api/v1/customers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstname", is("John")));
    }

    @Test
    void testGetCustomerById_NotFound() throws Exception {
        when(customerService.getCustomerById(2)).thenThrow(new CustomerNotFoundException(2));

        mockMvc.perform(get("/api/v1/customers/2"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateCustomer() throws Exception {
        CustomerRequest request = new CustomerRequest("John", "Doe", "john@gmail.com", "1295 Montague Expressway");
        CustomerResponse response = new CustomerResponse(1, "John", "Doe", "john@gmail.com", "1295 Montague Expressway", Role.ROLE_ADMIN);

        when(customerService.createCustomer(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstname", is("John")))
                .andExpect(jsonPath("$.lastname", is("Doe")));
    }

    @Test
    void testUpdateCustomer() throws Exception {
        CustomerRequest request = new CustomerRequest("Franklin", "Miller", "franklin@example.com", "1295 Montague Expressway");
        CustomerResponse response = new CustomerResponse(1, "Franklin", "Miller", "franklin@example.com", "1295 Montague Expressway", Role.ROLE_ADMIN);

        when(customerService.updateCustomer(eq(1), any())).thenReturn(response);

        mockMvc.perform(put("/api/v1/customers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstname", is("Franklin")));
    }

    @Test
    void testDeleteCustomer() throws Exception {
        doNothing().when(customerService).deleteCustomer(1);

        mockMvc.perform(delete("/api/v1/customers/1"))
                .andExpect(status().isNoContent());

        verify(customerService, times(1)).deleteCustomer(1);
    }
}
