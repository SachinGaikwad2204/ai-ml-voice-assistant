package com.aiml.voice.controller;

import com.aiml.voice.model.Conversation;
import com.aiml.voice.repository.ConversationRepository;
import com.aiml.voice.service.AIService;
import com.aiml.voice.service.AutomaticTrainingService;
import com.aiml.voice.service.TrainingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping(value = "/api/voice", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin(origins = "*")
public class VoiceController {
    @Autowired
    private AIService aiService;
    @Autowired
    private TrainingService trainingService;
    @Autowired
    private AutomaticTrainingService automaticTrainingService;
    @Autowired
    private ConversationRepository conversationRepository;
    
    @PostMapping(value = "/chat", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> chat(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        String sessionId = request.getOrDefault("sessionId", UUID.randomUUID().toString());
        String language = request.getOrDefault("language", "en");
        
        // Fix encoding issues - if message contains question marks or garbled text
        // and language is Hindi, try to decode it properly
        if (message != null && language.equals("hi")) {
            // Check if message is garbled (contains ??? or weird characters)
            if (message.contains("?") || message.matches(".*[^\\x00-\\x7F].*") == false) {
                // Try to recover the original message from the request
                // The message is already UTF-8, but PowerShell might be sending it wrong
                System.out.println("⚠️ WARNING: Hindi message may be garbled: " + message);
            }
        }
        
        System.out.println("=== CHAT REQUEST ===");
        System.out.println("Message: " + message);
        System.out.println("Language: " + language);
        System.out.println("Session: " + sessionId);
        
        Map<String, Object> response = aiService.processMessage(message, sessionId, language);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/history/{sessionId}")
    public ResponseEntity<List<Conversation>> getHistory(@PathVariable String sessionId) {
        return ResponseEntity.ok(aiService.getHistory(sessionId));
    }
    
    @PostMapping("/train")
    public ResponseEntity<Map<String, String>> trainModel() {
        String result = trainingService.trainModel();
        return ResponseEntity.ok(Map.of("status", result));
    }
    
    @PostMapping("/train/auto")
    public ResponseEntity<Map<String, String>> autoTrain() {
        String result = automaticTrainingService.trainNow();
        return ResponseEntity.ok(Map.of("status", result));
    }
    
    @GetMapping("/train/status")
    public ResponseEntity<Map<String, Object>> getTrainingStatus() {
        return ResponseEntity.ok(automaticTrainingService.getTrainingStatus());
    }
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "AI/ML Voice Assistant",
            "timestamp", String.valueOf(System.currentTimeMillis())
        ));
    }
    
    @GetMapping("/intents")
    public ResponseEntity<Map<String, Object>> getIntents() {
        Map<String, Object> result = new HashMap<>();
        result.put("intents", new String[]{
            "GREETING", "TIME", "WEATHER", "MUSIC", "HELP",
            "GOODBYE", "ABOUT", "AI", "NAME", "JOKE",
            "THANKYOU", "SEARCH", "CALCULATE", "TRANSLATE", "UNKNOWN"
        });
        return ResponseEntity.ok(result);
    }
}