package com.aiml.voice.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "conversations")
public class Conversation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "session_id", nullable = false)
    private String sessionId;
    
    @Column(name = "user_message", columnDefinition = "TEXT")
    private String userMessage;
    
    @Column(name = "assistant_response", columnDefinition = "TEXT")
    private String assistantResponse;
    
    @Column(name = "intent")
    private String intent;
    
    @Column(name = "language")
    private String language;
    
    @Column(name = "confidence")
    private Double confidence;
    
    @Column(name = "used_for_training", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean usedForTraining = false;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "audio_duration")
    private Double audioDuration;
    
    @Column(name = "sentiment")
    private String sentiment;
    
    @Column(name = "is_voice", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean isVoice = false;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (sessionId == null) {
            sessionId = UUID.randomUUID().toString();
        }
        // Ensure default values
        usedForTraining = false;
        isVoice = false;
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public Conversation() {
        this.usedForTraining = false;
        this.isVoice = false;
    }
    
    public Conversation(String sessionId, String userMessage, String assistantResponse) {
        this.sessionId = sessionId;
        this.userMessage = userMessage;
        this.assistantResponse = assistantResponse;
        this.usedForTraining = false;
        this.isVoice = false;
    }
    
    public Conversation(String sessionId, String userMessage, String assistantResponse, String intent) {
        this.sessionId = sessionId;
        this.userMessage = userMessage;
        this.assistantResponse = assistantResponse;
        this.intent = intent;
        this.usedForTraining = false;
        this.isVoice = false;
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    
    public String getUserMessage() { return userMessage; }
    public void setUserMessage(String userMessage) { this.userMessage = userMessage; }
    
    public String getAssistantResponse() { return assistantResponse; }
    public void setAssistantResponse(String assistantResponse) { this.assistantResponse = assistantResponse; }
    
    public String getIntent() { return intent; }
    public void setIntent(String intent) { this.intent = intent; }
    
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    
    public Double getConfidence() { return confidence; }
    public void setConfidence(Double confidence) { this.confidence = confidence; }
    
    public boolean isUsedForTraining() { return usedForTraining; }
    public void setUsedForTraining(boolean usedForTraining) { this.usedForTraining = usedForTraining; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public Double getAudioDuration() { return audioDuration; }
    public void setAudioDuration(Double audioDuration) { this.audioDuration = audioDuration; }
    
    public String getSentiment() { return sentiment; }
    public void setSentiment(String sentiment) { this.sentiment = sentiment; }
    
    public boolean isVoice() { return isVoice; }
    public void setVoice(boolean voice) { isVoice = voice; }
    
    public boolean hasValidData() {
        return userMessage != null && !userMessage.trim().isEmpty() 
                && intent != null && !intent.trim().isEmpty();
    }
    
    @Override
    public String toString() {
        return "Conversation{" +
                "id=" + id +
                ", sessionId='" + sessionId + '\'' +
                ", userMessage='" + userMessage + '\'' +
                ", intent='" + intent + '\'' +
                ", usedForTraining=" + usedForTraining +
                ", createdAt=" + createdAt +
                '}';
    }
}