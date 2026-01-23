package com.retail_project.it;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest
class PostgresIT {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("retail_db")
                    .withUsername("user")
                    .withPassword("password");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);

        //  Kafka/Redis are optional right now
        r.add("spring.kafka.bootstrap-servers", () -> "localhost:9092");
        r.add("spring.data.redis.host", () -> "localhost");
        r.add("spring.data.redis.port", () -> "6379");
    }

    @Test
    void contextLoads() {
        // If the Spring context starts + connects to Postgres, this passes.
    }
}

