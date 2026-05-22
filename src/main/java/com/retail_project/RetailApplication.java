package com.retail_project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class RetailApplication {

	public static void main(String[] args) {
		// Load .env values and push them into System Properties automatically
		Dotenv.configure()
				.systemProperties()
				.ignoreIfMissing()
				.load();

		SpringApplication.run(RetailApplication.class, args);
	}
}
