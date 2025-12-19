package com.retail_project.customer;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {
    // Only inject the service actually needed
    private final CustomerService customerService;
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Create customers")
    @PostMapping
    public ResponseEntity<CustomerResponse> createCustomer(@RequestBody CustomerRequest request){

        return ResponseEntity.ok(customerService.createCustomer(request));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Get all customers")
    @GetMapping
    public List<CustomerResponse> getCustomers(){

        return customerService.getCustomers();
    }

    @Operation(summary = "Get customer by ID")
    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> getCustomerById(@PathVariable int id ){
        return ResponseEntity.ok(customerService.getCustomerById(id));

    }

    @Operation(summary = "Update customer by ID")
    @PutMapping("/{id}")
    ResponseEntity<CustomerResponse> updateCustomer(@PathVariable int id,@RequestBody CustomerRequest request){

        return ResponseEntity.ok(customerService.updateCustomer(id, request));
    }

    @Operation(summary = "Delete customer by ID")
    @DeleteMapping("/{id}")
    ResponseEntity<?> deleteCustomer(@PathVariable int id){
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get current customer profile (requires authentication)")
    @GetMapping("/me")
    public ResponseEntity<CustomerResponse> getCustomerProfile(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        String email;
        if (principal instanceof Customer customer) {
            email = customer.getEmail();
        } else {
            email = authentication.getName();
        }
        System.out.println("Authentication email "+email);
        CustomerResponse profile = customerService.getCustomerByEmail(email);
        return ResponseEntity.ok(profile);
    }
    @PutMapping("/me")
    public ResponseEntity<CustomerResponse> updateProfile(@RequestBody Customer updated) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        CustomerResponse customerResponse = customerService.updateProfile(email, updated);

        return ResponseEntity.ok(customerResponse);
    }


}

