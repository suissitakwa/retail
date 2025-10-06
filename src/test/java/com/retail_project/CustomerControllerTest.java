package com.retail_project;


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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

@WebMvcTest(controllers = CustomerController.class,
excludeAutoConfiguration = {SecurityAutoConfiguration.class} )
public class CustomerControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
   private  CustomerService customerService;



    @Test
    void testGetCustomers() throws Exception {
        Customer customer=new Customer(1, "John", "Doe", "John@gmail.com", " 1295 Montague expressway,95146 California");
        when(customerService.getCustomers()).thenReturn(List.of(customer));

        mockMvc.perform(get("/api/v1/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstname").value("John"));
    }
    @Test
    void testGetCustomerById_Found() throws Exception {
        Customer customer=new Customer(1, "John", "Doe", "John@gmail.com", " 1295 Montague expressway,95146 California");
        when(customerService.getCustomerById(1)).thenReturn(Optional.of(customer));
        mockMvc.perform(get("/api/v1/customers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstname").value("John"));

    }
    @Test
    void testGetCustomerById_NotFound() throws Exception {
        when(customerService.getCustomerById(2)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/v1/customers/2"))
                .andExpect(status().isNotFound());
    }
    @Test
    void testCreateCustomer () throws Exception {
        Customer newCustomer = new Customer(1, "John", "Doe", "John@gmail.com", " 1295 Montague expressway,95146 California");
        when(customerService.createCustomer(any(Customer.class))) .thenReturn(newCustomer);
        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCustomer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstname", is("John")))
                .andExpect(jsonPath("$.lastname", is("Doe")));

    }
    @Test
    void testUpdateCustomer_Found() throws Exception {
        Customer updatedCustomer= new Customer(1,"Franklin", "Miller", "franklin@example.com"," 1295 Montague expressway,95146 California");
        when(customerService.updateCustomer(eq(1), any(Customer.class))).thenReturn(updatedCustomer);
        mockMvc.perform(put("/api/v1/customers/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedCustomer)))
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
