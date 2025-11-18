package com.retail_project.auth;

public record LoginRequest(
         String email,
         String password
) {
}
