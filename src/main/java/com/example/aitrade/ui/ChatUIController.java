package com.example.aitrade.ui;

import com.example.aitrade.model.ChatRequest;
import com.example.aitrade.model.ChatResponse;
import com.example.aitrade.service.ChatService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/")
public class ChatUIController {
    
    private static final Logger log = LoggerFactory.getLogger(ChatUIController.class);
    
    private final ChatService chatService;
    
    public ChatUIController(ChatService chatService) {
        this.chatService = chatService;
    }
    
    /**
     * Serve the main chat UI
     */
    @GetMapping("/")
    public String index() {
        log.info("Serving chat UI");
        return "forward:/index.html";
    }
    
    /**
     * Enhanced chat endpoint that handles the UI request format with userId and sessionId
     */
    @PostMapping("/api/chat/ui")
    @ResponseBody
    public ResponseEntity<ChatResponse> processUIMessage(@Valid @RequestBody ChatUIRequest request) {
        log.info("Processing UI message from user: {} in session: {} as {}", 
                request.getUserId(), request.getSessionId(), request.getUserType());
        
        // Convert UI request to internal ChatRequest format
        ChatRequest chatRequest = new ChatRequest();
        chatRequest.setMessage(request.getMessage());
        chatRequest.setSessionId(request.getSessionId());
        chatRequest.setUserType(request.getUserType());
        
        // Process with existing ChatService
        ChatResponse response = chatService.processMessage(chatRequest);
        
        // Enhance response with UI-specific data
        response.setUserId(request.getUserId());
        
        log.info("Processed UI message successfully for user: {}", request.getUserId());
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get current AI engine status for UI display
     */
    @GetMapping("/api/chat/engine-status")
    @ResponseBody
    public ResponseEntity<EngineStatus> getEngineStatus() {
        String activeEngine = System.getenv("ACTIVE_AI_ENGINE");
        if (activeEngine == null) {
            activeEngine = "ollama"; // default
        }
        
        EngineStatus status = new EngineStatus();
        status.setActiveEngine(activeEngine);
        status.setStatus("active");
        status.setTimestamp(java.time.LocalDateTime.now());
        
        return ResponseEntity.ok(status);
    }
    
    /**
     * Clear session endpoint for UI
     */
    @DeleteMapping("/api/chat/ui/session/{sessionId}")
    @ResponseBody
    public ResponseEntity<Void> clearUISession(@PathVariable String sessionId) {
        log.info("Clearing UI session: {}", sessionId);
        chatService.clearConversationHistory(sessionId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Request model for UI chat messages
     */
    public static class ChatUIRequest {
        private String message;
        private String sessionId;
        private String userId;
        private ChatRequest.UserType userType = ChatRequest.UserType.CUSTOMER;
        
        // Constructors
        public ChatUIRequest() {}
        
        public ChatUIRequest(String message, String sessionId, String userId, ChatRequest.UserType userType) {
            this.message = message;
            this.sessionId = sessionId;
            this.userId = userId;
            this.userType = userType;
        }
        
        // Getters and Setters
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
        
        public String getSessionId() {
            return sessionId;
        }
        
        public void setSessionId(String sessionId) {
            this.sessionId = sessionId;
        }
        
        public String getUserId() {
            return userId;
        }
        
        public void setUserId(String userId) {
            this.userId = userId;
        }
        
        public ChatRequest.UserType getUserType() {
            return userType;
        }
        
        public void setUserType(ChatRequest.UserType userType) {
            this.userType = userType;
        }
        
        @Override
        public String toString() {
            return "ChatUIRequest{" +
                    "message='" + message + '\'' +
                    ", sessionId='" + sessionId + '\'' +
                    ", userId='" + userId + '\'' +
                    ", userType=" + userType +
                    '}';
        }
    }
    
    /**
     * Engine status response model
     */
    public static class EngineStatus {
        private String activeEngine;
        private String status;
        private java.time.LocalDateTime timestamp;
        
        // Constructors
        public EngineStatus() {}
        
        // Getters and Setters
        public String getActiveEngine() {
            return activeEngine;
        }
        
        public void setActiveEngine(String activeEngine) {
            this.activeEngine = activeEngine;
        }
        
        public String getStatus() {
            return status;
        }
        
        public void setStatus(String status) {
            this.status = status;
        }
        
        public java.time.LocalDateTime getTimestamp() {
            return timestamp;
        }
        
        public void setTimestamp(java.time.LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }
        
        @Override
        public String toString() {
            return "EngineStatus{" +
                    "activeEngine='" + activeEngine + '\'' +
                    ", status='" + status + '\'' +
                    ", timestamp=" + timestamp +
                    '}';
        }
    }
}