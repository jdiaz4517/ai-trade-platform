package com.example.aitrade.config;

import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.ollama.OllamaChatClient;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.openai.OpenAiChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class ChatConfig {
    
    private static final Logger log = LoggerFactory.getLogger(ChatConfig.class);
    
    
    // Ollama Configuration
    @Value("${spring.ai.ollama.base-url}")
    private String ollamaBaseUrl;
    
    @Value("${spring.ai.ollama.chat.model}")
    private String ollamaModel;
    
    @Value("${spring.ai.ollama.chat.options.temperature}")
    private Double temperature;
    
    @Value("${spring.ai.ollama.chat.options.top-p}")
    private Double topP;
    
    // Groq Configuration (uses OpenAI-compatible API)
    @Value("${spring.ai.openai.api-key:}")
    private String groqApiKey;
    
    @Value("${spring.ai.openai.base-url}")
    private String groqBaseUrl;
    
    @Value("${spring.ai.openai.chat.model}")
    private String groqModel;
    
    @Bean
    @ConditionalOnProperty(name = "app.active-ai-engine", havingValue = "ollama", matchIfMissing = true)
    public OllamaApi ollamaApi() {
        log.info("Configuring Ollama API with base URL: {}", ollamaBaseUrl);
        return new OllamaApi(ollamaBaseUrl);
    }
    
    @Bean("chatClient")
    @Primary
    @ConditionalOnProperty(name = "app.active-ai-engine", havingValue = "ollama", matchIfMissing = true)
    public ChatClient ollamaChatClient(OllamaApi ollamaApi) {
        log.info("Creating Ollama ChatClient with model: {}", ollamaModel);
        return new OllamaChatClient(ollamaApi)
            .withDefaultOptions(OllamaOptions.create()
                .withModel(ollamaModel)
                .withTemperature(temperature.floatValue())
                .withTopP(topP.floatValue())
            );
    }
    
    @Bean
    @ConditionalOnProperty(name = "app.active-ai-engine", havingValue = "groq")
    public OpenAiApi groqApi() {
        log.info("Configuring Groq API with base URL: {}", groqBaseUrl);
        return new OpenAiApi(groqBaseUrl, groqApiKey);
    }
    
    @Bean("chatClient")
    @Primary
    @ConditionalOnProperty(name = "app.active-ai-engine", havingValue = "groq")
    public ChatClient groqChatClient(OpenAiApi groqApi) {
        log.info("Creating Groq ChatClient with model: {}", groqModel);
        return new OpenAiChatClient(groqApi,
            OpenAiChatOptions.builder()
                .withModel(groqModel)
                .withTemperature(temperature.floatValue())
                .withTopP(topP.floatValue())
                .build()
        );
    }
    
    // No explicit ChatClient bean needed - Spring will use the conditional beans above
}