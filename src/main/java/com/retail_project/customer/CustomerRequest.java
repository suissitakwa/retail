package com.retail_project.customer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CustomerRequest(
        @NotBlank(message = "Customer firstname is required")
        String firstname,
        @NotBlank(message = "Customer lastname is required")
        String lastname,
        @NotBlank(message = "Customer email is required")
        @Email(message="This should be an email format")
        String email,
        @NotBlank(message = "Customer address is required")
        String address
) {
}
