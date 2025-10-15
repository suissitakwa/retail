package com.retail_project.customer;

import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {

    public Customer toEntity(CustomerRequest request){
        return Customer.builder()
                        .firstname(request.firstname())
                        .lastname(request.lastname())
                       .email(request.email())
                       .address(request.address())
        .               build();

    }
    public CustomerResponse toResponse(Customer customer){
        return new CustomerResponse(customer.getId(),
                customer.getFirstname(),
                customer.getLastname(),
                customer.getEmail(),
                customer.getAddress());

    }

}
