package com.retail_project.auth;

import com.retail_project.customer.Role;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("itest")
@Testcontainers(disabledWithoutDocker = true)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:14-alpine");

    @Autowired
    private TestRestTemplate restTemplate;

    private static final String EMAIL = "integration@test.com";
    private static final String PASSWORD = "SecurePass1!";
    private static String savedRefreshToken;

    // --------------------------------------------------
    // TEST 1: Register a new customer
    // --------------------------------------------------
    @Test
    @Order(1)
    void register_returns_200_with_tokens() {
        var request = new RegisterRequest(EMAIL, PASSWORD, "John", "Doe",
                Role.ROLE_CUSTOMER, "123 Test Street");

        var response = restTemplate.postForEntity("/auth/register", request, AuthResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getToken()).isNotBlank();
        assertThat(response.getBody().getRefreshToken()).isNotBlank();

        savedRefreshToken = response.getBody().getRefreshToken();
    }

    // --------------------------------------------------
    // TEST 2: Duplicate email registration returns 400
    // --------------------------------------------------
    @Test
    @Order(2)
    void register_duplicate_email_returns_400() {
        var request = new RegisterRequest(EMAIL, PASSWORD, "Jane", "Doe",
                Role.ROLE_CUSTOMER, "456 Other Street");

        var response = restTemplate.postForEntity("/auth/register", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // --------------------------------------------------
    // TEST 3: Login with correct credentials
    // --------------------------------------------------
    @Test
    @Order(3)
    void login_with_valid_credentials_returns_tokens() {
        var request = new LoginRequest(EMAIL, PASSWORD);

        var response = restTemplate.postForEntity("/auth/login", request, AuthResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getToken()).isNotBlank();
        assertThat(response.getBody().getRefreshToken()).isNotBlank();
    }

    // --------------------------------------------------
    // TEST 4: Login with wrong password returns 401
    // --------------------------------------------------
    @Test
    @Order(4)
    void login_with_wrong_password_returns_401() {
        var request = new LoginRequest(EMAIL, "WrongPassword!");

        var response = restTemplate.postForEntity("/auth/login", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    // --------------------------------------------------
    // TEST 5: Refresh token issues new access token
    // --------------------------------------------------
    @Test
    @Order(5)
    void refresh_token_returns_new_access_token() {
        // Need a fresh refresh token — re-login
        var loginResp = restTemplate.postForEntity("/auth/login",
                new LoginRequest(EMAIL, PASSWORD), AuthResponse.class);
        String refreshToken = loginResp.getBody().getRefreshToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var body = Map.of("refreshToken", refreshToken);
        var response = restTemplate.postForEntity("/auth/refresh",
                new HttpEntity<>(body, headers), AuthResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getToken()).isNotBlank();
    }

    // --------------------------------------------------
    // TEST 6: Forgot password always returns 200
    // --------------------------------------------------
    @Test
    @Order(6)
    void forgot_password_always_returns_200() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Known email
        var resp1 = restTemplate.postForEntity("/auth/forgot-password",
                new HttpEntity<>(Map.of("email", EMAIL), headers), Void.class);
        assertThat(resp1.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Unknown email — must also return 200 (no user enumeration)
        var resp2 = restTemplate.postForEntity("/auth/forgot-password",
                new HttpEntity<>(Map.of("email", "nobody@test.com"), headers), Void.class);
        assertThat(resp2.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
