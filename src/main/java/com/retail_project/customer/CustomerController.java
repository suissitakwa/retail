package com.retail_project.customer;

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

    @PostMapping
    public Customer createCustomer(@RequestBody Customer customer){
        return customerService.createCustomer(customer);
    }
    //get all customers
    @Operation(summary = "Get all customers")
    @GetMapping
    public List<Customer> getCustomers(){
        return customerService.getCustomers();
    }
    // get customer by Id
    @Operation(summary = "Get customer by ID")
    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomerById(@PathVariable int id ){
       Optional<Customer>  customer= customerService.getCustomerById(id);
        return customer.map(ResponseEntity::ok)
                .orElseGet(()->ResponseEntity.notFound().build());

    }
    @Operation(summary = "Update customer by ID")
    @PutMapping("/{id}")
    ResponseEntity<Customer> updateCustomer(@PathVariable int id,@RequestBody Customer customer){
        Customer updatedCustomer =customerService.updateCustomer(id, customer);
        return ResponseEntity.ok(updatedCustomer);
    }
    @Operation(summary = "Delate customer by ID")
    @DeleteMapping("/{id}")
    ResponseEntity<?> deleteCustomer(@PathVariable int id){
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }

}
