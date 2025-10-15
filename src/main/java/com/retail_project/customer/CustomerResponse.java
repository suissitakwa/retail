package com.retail_project.customer;

public record CustomerResponse(
        Integer id,
        String firstname,
         String lastname,
        String email,
        String address

) {}
