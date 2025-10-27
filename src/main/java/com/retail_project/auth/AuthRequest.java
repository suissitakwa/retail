package com.retail_project.auth;

import lombok.Data;

@Data
public class AuthRequest {
    private String email;
    private String password;
}
