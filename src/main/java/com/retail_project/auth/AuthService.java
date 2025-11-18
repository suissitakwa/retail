package com.retail_project.auth;

import com.retail_project.config.jwt.JwtService;

import com.retail_project.config.jwt.MyUserDetails;
import com.retail_project.customer.Customer;
import com.retail_project.customer.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final CustomerRepository customerRepository;
    private final JwtService jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public String login(String email, String password) {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Customer not found"));

        if (!passwordEncoder.matches(password, customer.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }
        UserDetails userDetails=new MyUserDetails(customer);
        return jwtUtil.generateToken(userDetails);
    }

    public Customer getCurrentCustomer(String token) {
        String email = jwtUtil.extractEmail(token);
        return customerRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Customer not found"));
    }
}

