package com.retail_project.product;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for public product endpoints.
 * Uses a real PostgreSQL container + Flyway migrations.
 * Admin-only endpoints (POST/PUT/DELETE) are covered separately
 * once seeded admin user support is added via @Sql.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("itest")
@Testcontainers(disabledWithoutDocker = true)
class ProductIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:14-alpine");

    @Autowired
    private TestRestTemplate restTemplate;

    // --------------------------------------------------
    // TEST 1: GET all products — public, always returns 200
    // --------------------------------------------------
    @Test
    void get_all_products_returns_200() {
        var response = restTemplate.getForEntity("/api/v1/products", ProductResponse[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        // Fresh DB via Flyway — no products seeded, list is empty
        assertThat(response.getBody()).isEmpty();
    }

    // --------------------------------------------------
    // TEST 2: GET non-existent product returns 404
    // --------------------------------------------------
    @Test
    void get_product_by_invalid_id_returns_404() {
        var response = restTemplate.getForEntity("/api/v1/products/99999", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // --------------------------------------------------
    // TEST 3: POST without auth returns 401/403
    // --------------------------------------------------
    @Test
    void create_product_without_auth_returns_unauthorized() {
        var request = new ProductRequest("Laptop", "Gaming laptop",
                new java.math.BigDecimal("999.99"), 1, "");

        var response = restTemplate.postForEntity("/api/v1/products", request, String.class);

        // Security rejects unauthenticated requests
        assertThat(response.getStatusCode().value()).isIn(401, 403);
    }

    // --------------------------------------------------
    // TEST 4: GET all categories — public, always returns 200
    // --------------------------------------------------
    @Test
    void get_all_categories_returns_200() {
        var response = restTemplate.getForEntity("/api/v1/categories",
                com.retail_project.category.CategoryResponse[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }
}
