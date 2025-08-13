package com.example.aitrade.controller;

import com.example.aitrade.model.ChatRequest;
import com.example.aitrade.model.ChatResponse;
import com.example.aitrade.service.ChatService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {
    
    private static final Logger log = LoggerFactory.getLogger(ChatController.class);
    
    private final ChatService chatService;
    
    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }
    
    @PostMapping("/message")
    public ResponseEntity<ChatResponse> sendMessage(@Valid @RequestBody ChatRequest request) {
        log.info("Received chat message from user type: {}", request.getUserType());
        
        ChatResponse response = chatService.processMessage(request);
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/customer")
    public ResponseEntity<ChatResponse> customerMessage(@Valid @RequestBody ChatRequest request) {
        request.setUserType(ChatRequest.UserType.CUSTOMER);
        return sendMessage(request);
    }
    
    @PostMapping("/tradesperson")  
    public ResponseEntity<ChatResponse> tradespersonMessage(@Valid @RequestBody ChatRequest request) {
        request.setUserType(ChatRequest.UserType.TRADESPERSON);
        return sendMessage(request);
    }
    
    // ========== AI-POWERED EXTRACTION WITH THREE ENGINE SUPPORT ==========
    
    @PostMapping("/ai-extract")
    public ResponseEntity<ChatResponse> aiExtractionDemo(@Valid @RequestBody ChatRequest request) {
        log.info("Processing with AI-powered extraction using engine: {} for user type: {}", 
                System.getenv("ACTIVE_AI_ENGINE"), request.getUserType());
        ChatResponse response = chatService.processMessage(request);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/session/{sessionId}")
    public ResponseEntity<Void> clearSession(@PathVariable String sessionId) {
        log.info("Clearing session: {}", sessionId);
        chatService.clearConversationHistory(sessionId);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("AI Trade Platform Chat Service is running!");
    }
}