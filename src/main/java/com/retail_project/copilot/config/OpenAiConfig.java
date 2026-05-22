package com.retail_project.copilot.config;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAiConfig {

    @Bean
    public OpenAIClient openAIClient() {
        // System.getProperty first (set by java-dotenv at app startup via main()),
        // then fall back to environment variable (works in CI / test context)
        String apiKey = System.getProperty("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            apiKey = System.getenv("OPENAI_API_KEY");
        }
        if (apiKey == null || apiKey.isBlank()) {
            // No key available — return a dummy client so the context loads.
            // Any actual copilot call will fail at runtime, not at startup.
            apiKey = "sk-dummy-no-op-key-for-non-copilot-tests";
        }
        return OpenAIOkHttpClient.builder().apiKey(apiKey).build();
    }
}