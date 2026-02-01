package com.retail_project.copilot.config;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAiConfig {

    @Bean
    public OpenAIClient openAIClient() {

        String apiKey = System.getProperty("OPENAI_API_KEY");

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("OPENAI_API_KEY not found in System Properties. Check your .env file!");
        }
        return OpenAIOkHttpClient.builder().apiKey(apiKey).build();
    }
}