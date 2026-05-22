package com.retail_project.auth;

import com.retail_project.config.jwt.JwtService;
import com.retail_project.config.jwt.MyUserDetails;
import com.retail_project.customer.Customer;
import com.retail_project.customer.CustomerRepository;
import com.retail_project.customer.Role;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody @Valid RegisterRequest request) {
        if (customerRepository.existsByEmail(request.email())) {
            return ResponseEntity.badRequest().build();
        }
        Customer customer = Customer.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .firstname(request.firstname())
                .lastname(request.lastname())
                .role(Role.ROLE_CUSTOMER)
                .address(request.address())
                .build();
        customerRepository.save(customer);
        UserDetails userDetails = new MyUserDetails(customer);
        return ResponseEntity.ok(new AuthResponse(
                jwtService.generateToken(userDetails),
                jwtService.generateRefreshToken(userDetails)
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        return ResponseEntity.ok(new AuthResponse(
                jwtService.generateToken(userDetails),
                jwtService.generateRefreshToken(userDetails)
        ));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        String email = jwtService.extractEmail(refreshToken);
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Customer not found"));
        UserDetails userDetails = new MyUserDetails(customer);
        if (!jwtService.isTokenValid(refreshToken, userDetails)) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(new AuthResponse(
                jwtService.generateToken(userDetails),
                jwtService.generateRefreshToken(userDetails)
        ));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        customerRepository.findByEmail(email).ifPresent(customer -> {
            String token = UUID.randomUUID().toString();
            customer.setResetToken(token);
            customer.setResetTokenExpiry(LocalDateTime.now().plusHours(1));
            customerRepository.save(customer);
            // In production: send email with reset link containing the token
            System.out.println("Password reset token for " + email + ": " + token);
        });
        // Always return 200 to avoid user enumeration
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        String newPassword = body.get("newPassword");
        Customer customer = customerRepository.findByResetToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid or expired reset token"));
        if (customer.getResetTokenExpiry() == null || customer.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().build();
        }
        customer.setPassword(passwordEncoder.encode(newPassword));
        customer.setResetToken(null);
        customer.setResetTokenExpiry(null);
        customerRepository.save(customer);
        return ResponseEntity.ok().build();
    }
}
