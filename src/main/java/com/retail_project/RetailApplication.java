package com.retail_project;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
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
