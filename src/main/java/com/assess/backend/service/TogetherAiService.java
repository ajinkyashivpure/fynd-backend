package com.assess.backend.service;

import com.assess.backend.config.TogetherAiConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TogetherAiService {

    private final TogetherAiConfig config;
    private final RestTemplate restTemplate = new RestTemplate();

    public List<Double> generateEmbeddings(String text) {
        String url = config.getBaseUrl() + "/embeddings";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(config.getApiKey());
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("model", config.getEmbeddingModel());
        body.put("input", text);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            Map<String, Object> response= restTemplate.postForObject(url ,request, Map.class );
            if (response!=null && response.containsKey("data")) {
                List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");
                if (!data.isEmpty() && data.get(0).containsKey("embedding")) {
                    List<Double> embedding = (List<Double>) data.get(0).get("embedding");
                    return embedding;
                }
            }
            throw new RuntimeException("Could not parse embedding response");
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new RuntimeException("Failed to get embeddings: " + e.getResponseBodyAsString(), e);
        }
    }
}

