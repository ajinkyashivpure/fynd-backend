package com.assess.backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "together-ai")
@Data
public class TogetherAiConfig {
    private String apiKey;
    private String baseUrl;
    private String embeddingModel;
}