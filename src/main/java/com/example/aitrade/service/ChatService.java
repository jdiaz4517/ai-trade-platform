package com.example.aitrade.service;

import com.example.aitrade.model.ChatRequest;
import com.example.aitrade.model.ChatResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.ChatClient;

import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ChatService {
    
    private static final Logger log = LoggerFactory.getLogger(ChatService.class);
    
    private final ChatClient chatClient;
    
    @Autowired(required = false)
    private XaiChatService xaiChatService;
    
    @Value("${app.active-ai-engine:ollama}")
    private String activeAiEngine;
    
    public ChatService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }
    
    @Value("${app.ai-agent.system-message}")
    private String systemMessage;
    
    // In-memory conversation history (use Redis/Database in production)
    private final Map<String, List<Message>> conversationHistory = new ConcurrentHashMap<>();
    
    public ChatResponse processMessage(ChatRequest request) {
        log.info("Processing message for user type: {} with session: {}", 
                request.getUserType(), request.getSessionId());
        
        String sessionId = request.getSessionId() != null ? 
                request.getSessionId() : generateSessionId();
        
        // Build conversation context
        List<Message> messages = getOrCreateConversationHistory(sessionId);
        
        // Add user message
        messages.add(new UserMessage(request.getMessage()));
        
        // Create prompt with system message and conversation history
        List<Message> promptMessages = new ArrayList<>();
        promptMessages.add(new SystemMessage(getSystemMessageForUserType(request.getUserType())));
        promptMessages.addAll(messages);
        
        
        try {
            String responseMessage;
            
            // Use different AI service based on configuration
            if ("grok".equals(activeAiEngine) && xaiChatService != null) {
                log.info("Using custom xAI service for engine: {}", activeAiEngine);
                // Create a combined message with system prompt and user message
                String combinedMessage = getSystemMessageForUserType(request.getUserType()) + "\n\nUser: " + request.getMessage();
                responseMessage = xaiChatService.callXaiApi(combinedMessage);
            } else {
                log.info("Using Spring AI ChatClient for engine: {}", activeAiEngine);
                // Call via Spring AI (supports both Ollama and Groq)
                org.springframework.ai.chat.ChatResponse aiResponse = chatClient.call(new Prompt(promptMessages));
                responseMessage = aiResponse.getResult().getOutput().getContent();
            }
            
            // Add AI response to conversation history
            messages.add(new SystemMessage(responseMessage));
            
            // Extract structured information from the conversation
            Map<String, Object> extractedInfo = extractInformation(request, responseMessage);
            
            return ChatResponse.builder()
                    .message(responseMessage)
                    .sessionId(sessionId)
                    .timestamp(LocalDateTime.now())
                    .extractedInfo(extractedInfo)
                    .requiresMoreInfo(determineIfMoreInfoNeeded(extractedInfo, request.getUserType()))
                    .nextAction(determineNextAction(extractedInfo, request.getUserType()))
                    .build();
                    
        } catch (Exception e) {
            log.error("Error processing chat message", e);
            return ChatResponse.builder()
                    .message("I'm sorry, I'm having trouble processing your request right now. Please try again.")
                    .sessionId(sessionId)
                    .timestamp(LocalDateTime.now())
                    .extractedInfo(new HashMap<>())
                    .requiresMoreInfo(false)
                    .nextAction("retry")
                    .build();
        }
    }
    
    private List<Message> getOrCreateConversationHistory(String sessionId) {
        return conversationHistory.computeIfAbsent(sessionId, k -> new ArrayList<>());
    }
    
    private String getSystemMessageForUserType(ChatRequest.UserType userType) {
        return systemMessage + "\n\nCurrent user type: " + userType.name();
    }
    
    private Map<String, Object> extractInformation(ChatRequest request, String aiResponse) {
        Map<String, Object> info = new HashMap<>();
        
        try {
            String extractionPrompt = createExtractionPrompt(request, aiResponse);
            String extractedJson;
            
            // Use the same AI engine for extraction
            if ("grok".equals(activeAiEngine) && xaiChatService != null) {
                extractedJson = xaiChatService.callXaiApi(extractionPrompt);
            } else {
                List<Message> extractionMessages = List.of(new UserMessage(extractionPrompt));
                org.springframework.ai.chat.ChatResponse extractionResponse = chatClient.call(new Prompt(extractionMessages));
                extractedJson = extractionResponse.getResult().getOutput().getContent();
            }
            
            // Parse AI-extracted information
            info = parseExtractedInformation(extractedJson, request.getUserType());
            
        } catch (Exception e) {
            log.error("Error extracting information with AI, falling back to basic extraction", e);
            // Fallback to basic extraction if AI fails
            info = basicInformationExtraction(request);
        }
        
        info.put("messageLength", request.getMessage().length());
        info.put("timestamp", LocalDateTime.now());
        
        return info;
    }
    
    private String createExtractionPrompt(ChatRequest request, String aiResponse) {
        String userType = request.getUserType().name().toLowerCase();
        
        if (request.getUserType() == ChatRequest.UserType.CUSTOMER) {
            return "Extract structured information from this customer conversation. Return ONLY a JSON object with these fields:\n" +
                   "{\n" +
                   "  \"serviceType\": \"Plumbing|Electrical|Painting|Carpentry|Gardening|General|Other\",\n" +
                   "  \"urgency\": \"Low|Medium|High\",\n" +
                   "  \"location\": \"extracted location or null\",\n" +
                   "  \"budget\": \"extracted budget or null\",\n" +
                   "  \"hasBudget\": true/false,\n" +
                   "  \"specificNeeds\": \"brief description or null\"\n" +
                   "}\n\n" +
                   "Customer message: " + request.getMessage() + "\n" +
                   "AI response: " + aiResponse + "\n\n" +
                   "Return only the JSON object:";
        } else {
            return "Extract structured information from this tradesperson conversation. Return ONLY a JSON object with these fields:\n" +
                   "{\n" +
                   "  \"tradeSkills\": [\"list of mentioned skills\"],\n" +
                   "  \"qualified\": true/false,\n" +
                   "  \"availability\": \"Available|Busy|Unknown\",\n" +
                   "  \"serviceAreas\": [\"list of mentioned areas\"],\n" +
                   "  \"experienceLevel\": \"Beginner|Intermediate|Expert|Unknown\"\n" +
                   "}\n\n" +
                   "Tradesperson message: " + request.getMessage() + "\n" +
                   "AI response: " + aiResponse + "\n\n" +
                   "Return only the JSON object:";
        }
    }
    
    private Map<String, Object> parseExtractedInformation(String extractedJson, ChatRequest.UserType userType) {
        Map<String, Object> info = new HashMap<>();
        
        try {
            // Simple JSON parsing - clean up the response
            String cleanJson = extractedJson.trim();
            if (cleanJson.startsWith("```json")) {
                cleanJson = cleanJson.substring(7);
            }
            if (cleanJson.endsWith("```")) {
                cleanJson = cleanJson.substring(0, cleanJson.length() - 3);
            }
            cleanJson = cleanJson.trim();
            
            // Parse basic fields from JSON string (simple approach)
            if (userType == ChatRequest.UserType.CUSTOMER) {
                info.put("serviceType", extractJsonField(cleanJson, "serviceType"));
                info.put("urgency", extractJsonField(cleanJson, "urgency"));
                info.put("location", extractJsonField(cleanJson, "location"));
                info.put("budget", extractJsonField(cleanJson, "budget"));
                info.put("hasBudget", extractJsonField(cleanJson, "hasBudget"));
                info.put("specificNeeds", extractJsonField(cleanJson, "specificNeeds"));
            } else {
                info.put("tradeSkills", extractJsonField(cleanJson, "tradeSkills"));
                info.put("qualified", extractJsonField(cleanJson, "qualified"));
                info.put("availability", extractJsonField(cleanJson, "availability"));
                info.put("serviceAreas", extractJsonField(cleanJson, "serviceAreas"));
                info.put("experienceLevel", extractJsonField(cleanJson, "experienceLevel"));
            }
            
        } catch (Exception e) {
            log.error("Error parsing extracted JSON: " + extractedJson, e);
            return basicInformationExtraction(null);
        }
        
        return info;
    }
    
    private String extractJsonField(String json, String fieldName) {
        try {
            // Simple regex-based field extraction
            String pattern = "\"" + fieldName + "\"\\s*:\\s*\"?([^,}\"]+)\"?";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(json);
            if (m.find()) {
                String value = m.group(1).trim();
                if ("null".equals(value)) return null;
                if ("true".equals(value)) return "true";
                if ("false".equals(value)) return "false";
                return value;
            }
        } catch (Exception e) {
            log.warn("Error extracting field {}: {}", fieldName, e.getMessage());
        }
        return null;
    }
    
    private Map<String, Object> basicInformationExtraction(ChatRequest request) {
        Map<String, Object> info = new HashMap<>();
        
        if (request != null && request.getUserType() == ChatRequest.UserType.CUSTOMER) {
            String lowerMessage = request.getMessage().toLowerCase();
            
            // Basic service type detection
            if (lowerMessage.contains("plumber") || lowerMessage.contains("plumbing")) {
                info.put("serviceType", "Plumbing");
            } else if (lowerMessage.contains("electrician") || lowerMessage.contains("electrical")) {
                info.put("serviceType", "Electrical");
            } else if (lowerMessage.contains("painter") || lowerMessage.contains("painting")) {
                info.put("serviceType", "Painting");
            }
            
            // Basic urgency detection
            if (lowerMessage.contains("urgent") || lowerMessage.contains("emergency")) {
                info.put("urgency", "High");
            }
        }
        
        return info;
    }
    
    private boolean determineIfMoreInfoNeeded(Map<String, Object> extractedInfo, ChatRequest.UserType userType) {
        if (userType == ChatRequest.UserType.CUSTOMER) {
            return !extractedInfo.containsKey("serviceType") || 
                   !extractedInfo.containsKey("urgency");
        } else {
            return !extractedInfo.containsKey("qualified") || 
                   !extractedInfo.containsKey("availability");
        }
    }
    
    private String determineNextAction(Map<String, Object> extractedInfo, ChatRequest.UserType userType) {
        if (determineIfMoreInfoNeeded(extractedInfo, userType)) {
            return "gather_more_info";
        } else if (userType == ChatRequest.UserType.CUSTOMER) {
            return "find_tradespeople";
        } else {
            return "show_job_opportunities";
        }
    }
    
    private String generateSessionId() {
        return "session_" + System.currentTimeMillis();
    }
    
    public void clearConversationHistory(String sessionId) {
        conversationHistory.remove(sessionId);
        log.info("Cleared conversation history for session: {}", sessionId);
    }
}