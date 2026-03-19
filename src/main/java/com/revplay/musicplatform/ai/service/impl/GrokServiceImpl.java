package com.revplay.musicplatform.ai.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.revplay.musicplatform.ai.service.GrokService;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class GrokServiceImpl implements GrokService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GrokServiceImpl.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String apiUrl;
    private final String apiKey;
    private final String modelName;

    public GrokServiceImpl(
            ObjectMapper objectMapper,
            @Value("${ai.url}") String apiUrl,
            @Value("${ai.api-key:}") String apiKey,
            @Value("${ai.model:grok-4}") String modelName
    ) {
        this.restTemplate = new RestTemplate();
        this.objectMapper = objectMapper;
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
        this.modelName = modelName;
    }

    @Override
    public String generateResponse(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            return null;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> requestBody = Map.of(
                "model", modelName,
                "messages", List.of(Map.of(
                        "role", "user",
                        "content", prompt
                ))
        );

        try {
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, request, String.class);
            return extractContent(response.getBody());
        } catch (RestClientException exception) {
            LOGGER.error("Failed to fetch AI response from provider endpoint: {}", apiUrl, exception);
            return null;
        }
    }

    private String extractContent(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            return null;
        }
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode contentNode = root.path("choices").path(0).path("message").path("content");
            if (contentNode.isMissingNode() || contentNode.isNull()) {
                return null;
            }
            String content = contentNode.asText();
            return content == null || content.isBlank() ? null : content.trim();
        } catch (JsonProcessingException exception) {
            LOGGER.error("Failed to parse AI provider response", exception);
            return null;
        }
    }
}
