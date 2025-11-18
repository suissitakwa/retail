package com.retail_project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import io.github.cdimascio.dotenv.Dotenv;
@SpringBootApplication
@EnableJpaAuditing
public class RetailApplication {

	public static void main(String[] args) {
		// Load .env values
		Dotenv dotenv = Dotenv.configure()
				.ignoreIfMissing()
				.load();

		// Set as system properties so Spring Boot can read them
		System.setProperty("DB_USERNAME", dotenv.get("DB_USERNAME"));
		System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD"));
		SpringApplication.run(RetailApplication.class, args);
	}

}
