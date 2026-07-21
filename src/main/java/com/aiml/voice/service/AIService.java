package com.aiml.voice.service;

import com.aiml.voice.model.Conversation;
import com.aiml.voice.repository.ConversationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class AIService {
    @Autowired
    private IntentClassifierService intentClassifierService;
    @Autowired
    private ConversationRepository conversationRepository;
    
    public Map<String, Object> processMessage(String message, String sessionId, String language) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String intent = "UNKNOWN";
            String response = "";
            
            System.out.println("=== MESSAGE RECEIVED ===");
            System.out.println("Message: " + message);
            System.out.println("Language: " + language);
            System.out.println("Session: " + sessionId);
            
            // Check if language is Hindi
            if ("hi".equals(language)) {
                System.out.println("Processing as HINDI message");
                // Simple Hindi detection
                if (message != null) {
                    if (message.contains("नमस्ते") || message.contains("नमस्कार") || message.contains("हाय")) {
                        intent = "GREETING";
                        response = "नमस्ते! मैं आपकी कैसे मदद कर सकता हूँ? 😊";
                    } else if (message.contains("समय") || message.contains("टाइम")) {
                        intent = "TIME";
                        response = "वर्तमान समय है: " + new Date();
                    } else if (message.contains("मौसम") || message.contains("बारिश")) {
                        intent = "WEATHER";
                        response = "मैं मौसम की जाँच कर रहा हूँ। आज का मौसम बहुत सुंदर है! ☀️";
                    } else if (message.contains("गाना") || message.contains("संगीत") || message.contains("बजाओ")) {
                        intent = "MUSIC";
                        response = "🎵 आपके लिए बढ़िया संगीत बजा रहा हूँ!";
                    } else if (message.contains("मदद") || message.contains("सहायता")) {
                        intent = "HELP";
                        response = "मैं मदद के लिए यहाँ हूँ! मैं मौसम, समय, संगीत, चुटकुले और बहुत कुछ में सहायता कर सकता हूँ!";
                    } else if (message.contains("मजाक") || message.contains("चुटकुला")) {
                        intent = "JOKE";
                        response = "वैज्ञानिक परमाणुओं पर भरोसा क्यों नहीं करते? क्योंकि वे सब कुछ बनाते हैं! 😂";
                    } else if (message.contains("अलविदा") || message.contains("बाय")) {
                        intent = "GOODBYE";
                        response = "अलविदा! आपका दिन शुभ हो! 🌟";
                    } else if (message.contains("धन्यवाद") || message.contains("शुक्रिया")) {
                        intent = "THANKYOU";
                        response = "आपका स्वागत है! मैं कभी भी मदद के लिए यहाँ हूँ! 😊";
                    } else if (message.contains("नाम") || message.contains("कौन")) {
                        intent = "NAME";
                        response = "मेरा नाम VoiceAI है! आपसे मिलकर अच्छा लगा! 🤖";
                    } else if (message.contains("क्या हो") || message.contains("बारे में")) {
                        intent = "ABOUT";
                        response = "मैं Java और Spring Boot के साथ बनाया गया AI/ML वॉयस सहायक हूँ! 🚀";
                    } else {
                        response = "मुझे समझ नहीं आया। क्या आप फिर से कह सकते हैं?";
                    }
                }
                System.out.println("Hindi Intent: " + intent);
            } else {
                // English processing
                System.out.println("Processing as ENGLISH message");
                String lowerMsg = message.toLowerCase();
                
                if (lowerMsg.contains("hello") || lowerMsg.contains("hi") || lowerMsg.contains("hey")) {
                    intent = "GREETING";
                    response = "Hello! How can I help you today? 😊";
                } else if (lowerMsg.contains("time") || lowerMsg.contains("clock")) {
                    intent = "TIME";
                    response = "The current time is: " + new Date();
                } else if (lowerMsg.contains("weather") || lowerMsg.contains("rain")) {
                    intent = "WEATHER";
                    response = "I'm checking the weather. It looks beautiful today! ☀️";
                } else if (lowerMsg.contains("music") || lowerMsg.contains("song") || lowerMsg.contains("play")) {
                    intent = "MUSIC";
                    response = "🎵 Playing some great music for you!";
                } else if (lowerMsg.contains("help") || lowerMsg.contains("assist")) {
                    intent = "HELP";
                    response = "I'm here to help! I can assist with weather, time, music, jokes, and more!";
                } else if (lowerMsg.contains("joke") || lowerMsg.contains("funny") || lowerMsg.contains("laugh")) {
                    intent = "JOKE";
                    response = "Why don't scientists trust atoms? Because they make up everything! 😂";
                } else if (lowerMsg.contains("bye") || lowerMsg.contains("goodbye") || lowerMsg.contains("see you")) {
                    intent = "GOODBYE";
                    response = "Goodbye! Have a wonderful day! 🌟";
                } else if (lowerMsg.contains("thank") || lowerMsg.contains("thanks")) {
                    intent = "THANKYOU";
                    response = "You're welcome! I'm here to help anytime! 😊";
                } else if (lowerMsg.contains("name") || lowerMsg.contains("who are you")) {
                    intent = "NAME";
                    response = "My name is VoiceAI! Nice to meet you! 🤖";
                } else if (lowerMsg.contains("about") || lowerMsg.contains("what are you")) {
                    intent = "ABOUT";
                    response = "I'm an AI/ML Voice Assistant built with Java and Spring Boot! 🚀";
                } else {
                    response = "I'm not sure I understand. Can you rephrase?";
                }
                System.out.println("English Intent: " + intent);
            }
            
            // Save conversation
            Conversation conversation = new Conversation(sessionId, message, response, intent);
            conversation.setLanguage(language);
            conversation.setConfidence(0.85);
            conversation.setUsedForTraining(false);
            conversation.setVoice(false);
            conversationRepository.save(conversation);
            
            result.put("message", message);
            result.put("response", response);
            result.put("intent", intent);
            result.put("sessionId", sessionId);
            result.put("timestamp", new Date());
            result.put("language", language);
            result.put("confidence", 0.85);
            
        } catch (Exception e) {
            e.printStackTrace();
            result.put("message", message);
            result.put("response", "Error processing your request");
            result.put("intent", "ERROR");
            result.put("sessionId", sessionId);
            result.put("timestamp", new Date());
            result.put("error", e.getMessage());
        }
        
        return result;
    }
    
    public List<Conversation> getHistory(String sessionId) {
        return conversationRepository.findBySessionIdOrderByCreatedAtDesc(sessionId);
    }
}