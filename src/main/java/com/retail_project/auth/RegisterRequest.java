package com.retail_project.auth;

import com.retail_project.customer.Role;

public record RegisterRequest(
         String email,
         String password,
         String firstname,
         String lastname,
         Role role
) {
}
