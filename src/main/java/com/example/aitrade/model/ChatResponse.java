package com.example.aitrade.model;

import java.time.LocalDateTime;
import java.util.Map;

public class ChatResponse {
    
    private String message;
    
    private String sessionId;
    
    private String userId;
    
    private LocalDateTime timestamp;
    
    private Map<String, Object> extractedInfo;
    
    private String nextAction;
    
    private boolean requiresMoreInfo;
    
    public ChatResponse() {}
    
    public ChatResponse(String message, String sessionId, LocalDateTime timestamp, 
                       Map<String, Object> extractedInfo, String nextAction, boolean requiresMoreInfo) {
        this.message = message;
        this.sessionId = sessionId;
        this.timestamp = timestamp;
        this.extractedInfo = extractedInfo;
        this.nextAction = nextAction;
        this.requiresMoreInfo = requiresMoreInfo;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
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
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public Map<String, Object> getExtractedInfo() {
        return extractedInfo;
    }
    
    public void setExtractedInfo(Map<String, Object> extractedInfo) {
        this.extractedInfo = extractedInfo;
    }
    
    public String getNextAction() {
        return nextAction;
    }
    
    public void setNextAction(String nextAction) {
        this.nextAction = nextAction;
    }
    
    public boolean isRequiresMoreInfo() {
        return requiresMoreInfo;
    }
    
    public void setRequiresMoreInfo(boolean requiresMoreInfo) {
        this.requiresMoreInfo = requiresMoreInfo;
    }
    
    public static class Builder {
        private String message;
        private String sessionId;
        private LocalDateTime timestamp;
        private Map<String, Object> extractedInfo;
        private String nextAction;
        private boolean requiresMoreInfo;
        
        public Builder message(String message) {
            this.message = message;
            return this;
        }
        
        public Builder sessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }
        
        public Builder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        
        public Builder extractedInfo(Map<String, Object> extractedInfo) {
            this.extractedInfo = extractedInfo;
            return this;
        }
        
        public Builder nextAction(String nextAction) {
            this.nextAction = nextAction;
            return this;
        }
        
        public Builder requiresMoreInfo(boolean requiresMoreInfo) {
            this.requiresMoreInfo = requiresMoreInfo;
            return this;
        }
        
        public ChatResponse build() {
            return new ChatResponse(message, sessionId, timestamp, extractedInfo, nextAction, requiresMoreInfo);
        }
    }
}