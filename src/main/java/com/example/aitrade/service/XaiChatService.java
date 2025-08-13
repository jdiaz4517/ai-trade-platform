package com.example.aitrade.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@ConditionalOnProperty(name = "app.active-ai-engine", havingValue = "grok")
public class XaiChatService {

    private static final Logger log = LoggerFactory.getLogger(XaiChatService.class);

    @Value("${XAI_API_KEY}")
    private String apiKey;

    @Value("${XAI_BASE_URL}")
    private String baseUrl;

    @Value("${XAI_MODEL}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String callXaiApi(String message) {
        try {
            // Create request payload
            XaiRequest request = new XaiRequest();
            request.model = model;
            request.messages = List.of(new XaiMessage("user", message));
            request.temperature = 0.7;
            request.stream = false;

            // Create headers
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            headers.set("Authorization", "Bearer " + apiKey);

            // Create HTTP entity
            String requestBody = objectMapper.writeValueAsString(request);
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

            log.info("Calling xAI API at: {}/chat/completions", baseUrl);
            log.debug("Request payload: {}", requestBody);

            // Make API call - using the exact URL that works in your curl
            String apiUrl = "https://api.x.ai/v1/chat/completions";
            log.info("Making request to: {}", apiUrl);
            
            ResponseEntity<String> response = restTemplate.exchange(
                apiUrl,
                HttpMethod.POST,
                entity,
                String.class
            );

            log.debug("xAI API response: {}", response.getBody());

            // Parse response
            Map<String, Object> responseMap = objectMapper.readValue(response.getBody(), Map.class);
            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
            if (!choices.isEmpty()) {
                Map<String, Object> message1 = (Map<String, Object>) choices.get(0).get("message");
                return (String) message1.get("content");
            }

            return "Sorry, I couldn't generate a response.";

        } catch (Exception e) {
            log.error("Error calling xAI API", e);
            return "Error: " + e.getMessage();
        }
    }

    // DTOs for xAI API
    public static class XaiRequest {
        public String model;
        public List<XaiMessage> messages;
        public double temperature;
        public boolean stream;
    }

    public static class XaiMessage {
        public String role;
        public String content;

        public XaiMessage(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }
}