package com.retail_project.customer;

import com.retail_project.auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.websocket.server.PathParam;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {
   private final CustomerService customerService;
    private final AuthService authService;
    @Operation(summary = "Create customers")
    @PostMapping
    public ResponseEntity<CustomerResponse> createCustomer(@RequestBody CustomerRequest request){

        return ResponseEntity.ok(customerService.createCustomer(request));
    }
    //get all customers
    @Operation(summary = "Get all customers")
    @GetMapping
    public List<CustomerResponse> getCustomers(){

        return customerService.getCustomers();
    }
    // get customer by Id
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
    @GetMapping("/me")
    public ResponseEntity<Customer> getProfile(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        Customer customer = authService.getCurrentCustomer(token);
        return ResponseEntity.ok(customer);
    }


}
