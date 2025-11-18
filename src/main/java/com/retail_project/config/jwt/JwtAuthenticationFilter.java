package com.retail_project.config.jwt;

import com.retail_project.customer.Customer;
import com.retail_project.customer.CustomerRepository;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtUtil;
    private final CustomerRepository customerRepository;

    public JwtAuthenticationFilter(JwtService jwtUtil, CustomerRepository customerRepository) {
        this.jwtUtil = jwtUtil;
        this.customerRepository = customerRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        String email = null;
        String token = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            email = jwtUtil.extractEmail(token);
        }

        System.out.println(" Incoming request: " + request.getRequestURI());
        System.out.println(" Token: " + token);
        System.out.println(" Extracted email: " + email);

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            var customerOpt = customerRepository.findByEmail(email);

            if (customerOpt.isPresent()) {
                var customer = customerOpt.get();
                var userDetails = new MyUserDetails(customer);
                boolean valid = jwtUtil.isTokenValid(token, userDetails);

                System.out.println(" Token valid? " + valid);

                if (valid) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    System.out.println("Token validation failed for " + email);
                }
            } else {
                System.out.println(" No customer found for email: " + email);
            }
        }

        filterChain.doFilter(request, response);
    }

}
