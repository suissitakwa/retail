package com.retail_project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
@EnableJpaAuditing
public class RetailApplication {

	public static void main(String[] args) {
		// Load .env values
		Dotenv dotenv = Dotenv.configure()
				.ignoreIfMissing()
				.load();

		setIfPresent("STRIPE_SECRET_KEY", dotenv.get("STRIPE_SECRET_KEY"));
		setIfPresent("DB_USERNAME", dotenv.get("DB_USERNAME"));
		setIfPresent("DB_PASSWORD", dotenv.get("DB_PASSWORD"));
		setIfPresent("STRIPE_WEBHOOK_SECRET", dotenv.get("STRIPE_WEBHOOK_SECRET"));
		SpringApplication.run(RetailApplication.class, args);
	}
	private static void setIfPresent(String key, String value) {
		if (value != null) {
			System.setProperty(key, value);
		}
	}
}
