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
    
    private static final Map<String, String[]> HINDI_KEYWORDS = new HashMap<>();
    private static final Map<String, String[]> RESPONSES = new HashMap<>();
    
    static {
        // Hindi keywords with more variations
        HINDI_KEYWORDS.put("GREETING", new String[]{
            "नमस्ते", "नमस्कार", "हेलो", "हाय", "प्रणाम", 
            "गुड मॉर्निंग", "शुभ प्रभात", "नमस्ते जी", "कैसे हो", 
            "कैसे हैं", "आप कैसे हैं", "क्या हाल है"
        });
        
        HINDI_KEYWORDS.put("TIME", new String[]{
            "समय", "घड़ी", "घंटा", "मिनट", "टाइम", "वक्त", 
            "कितना बजा", "बजे", "समय क्या है", "क्या समय",
            "समय बताओ", "वर्तमान समय"
        });
        
        HINDI_KEYWORDS.put("WEATHER", new String[]{
            "मौसम", "बारिश", "धूप", "बादल", "तापमान", 
            "गर्मी", "सर्दी", "ठंड", "मौसम कैसा", 
            "कैसा मौसम", "आज मौसम", "मौसम बताओ"
        });
        
        HINDI_KEYWORDS.put("MUSIC", new String[]{
            "संगीत", "गाना", "बजाओ", "सुनो", "प्लेलिस्ट", 
            "म्यूजिक", "गीत", "संगीत बजाओ", "गाना बजाओ"
        });
        
        HINDI_KEYWORDS.put("HELP", new String[]{
            "मदद", "सहायता", "सपोर्ट", "सलाह", "समस्या", 
            "मुश्किल", "मदद करो", "मेरी मदद करो", 
            "क्या आप मदद कर सकते हैं"
        });
        
        HINDI_KEYWORDS.put("JOKE", new String[]{
            "मजाक", "चुटकुला", "हँसी", "हास्य", "मुझे हँसाओ", 
            "मजेदार", "मजाक सुनाओ", "चुटकुला सुनाओ",
            "कुछ मजेदार बताओ"
        });
        
        HINDI_KEYWORDS.put("GOODBYE", new String[]{
            "अलविदा", "फिर मिलेंगे", "बाय", "गुडबाय", 
            "ध्यान रखना", "जल्द ही मिलेंगे", "तब तक"
        });
        
        HINDI_KEYWORDS.put("THANKYOU", new String[]{
            "धन्यवाद", "शुक्रिया", "थैंक्स", "आभार", 
            "बहुत धन्यवाद", "थैंक यू"
        });
        
        HINDI_KEYWORDS.put("NAME", new String[]{
            "नाम", "आपका नाम", "क्या नाम है", "कौन हो", 
            "आप कौन हैं", "नाम बताओ", "आपको क्या कहते हैं"
        });
        
        HINDI_KEYWORDS.put("ABOUT", new String[]{
            "क्या हो", "बारे में", "अपने बारे में बताओ", 
            "क्या कर सकते हो", "तुम क्या हो", "क्या करते हो",
            "अपने बारे में", "क्या कर सकते हैं"
        });
        
        // Hindi responses
        RESPONSES.put("GREETING_HI", new String[]{
            "नमस्ते! मैं आपकी कैसे मदद कर सकता हूँ? 😊",
            "नमस्कार! मैं आपके लिए क्या कर सकता हूँ?",
            "हे! आपको देखकर अच्छा लगा! मैं आपकी कैसे सहायता कर सकता हूँ?",
            "नमस्ते! आज मैं आपकी कैसे मदद कर सकता हूँ?"
        });
        
        RESPONSES.put("TIME_HI", new String[]{
            "वर्तमान समय है: " + new Date(),
            "अभी " + new Date() + " बज रहे हैं।",
            "समय " + new Date() + " है"
        });
        
        RESPONSES.put("WEATHER_HI", new String[]{
            "मैं मौसम की जाँच कर रहा हूँ। आज का मौसम बहुत सुंदर है! ☀️",
            "मौसम पूर्वानुमान साफ आसमान और सुहावना मौसम दिखा रहा है।",
            "मौसम बहुत अच्छा है! आज का दिन शानदार है।"
        });
        
        RESPONSES.put("MUSIC_HI", new String[]{
            "🎵 आपके लिए बढ़िया संगीत बजा रहा हूँ!",
            "आपके लिए सही प्लेलिस्ट ढूंढता हूँ! 🎶",
            "मुझे संगीत बजाना अच्छा लगता है! आनंद लें! 🎵"
        });
        
        RESPONSES.put("HELP_HI", new String[]{
            "मैं मदद के लिए यहाँ हूँ! मैं मौसम, समय, संगीत, चुटकुले और बहुत कुछ में सहायता कर सकता हूँ!",
            "आज मैं आपकी क्या मदद कर सकता हूँ? बस मुझसे कुछ भी पूछें!",
            "मैं आपका व्यक्तिगत सहायक हूँ! मैं आपके लिए विभिन्न कार्य कर सकता हूँ।"
        });
        
        RESPONSES.put("JOKE_HI", new String[]{
            "वैज्ञानिक परमाणुओं पर भरोसा क्यों नहीं करते? क्योंकि वे सब कुछ बनाते हैं! 😂",
            "उस भालू को क्या कहते हैं जिसके दाँत नहीं हैं? गमी भालू! 🐻",
            "मुर्गी ने सड़क क्यों पार की? दूसरी तरफ जाने के लिए! 🐔"
        });
        
        RESPONSES.put("GOODBYE_HI", new String[]{
            "अलविदा! आपका दिन शुभ हो! 🌟",
            "फिर मिलेंगे! अपना ख्याल रखें!",
            "अभी के लिए अलविदा! कभी भी वापस आएं!",
            "अगली बार तक! आपका दिन अच्छा हो!"
        });
        
        RESPONSES.put("THANKYOU_HI", new String[]{
            "आपका स्वागत है! मैं कभी भी मदद के लिए यहाँ हूँ! 😊",
            "मुझे खुशी हुई! अगर कुछ और चाहिए तो बताएं!",
            "कभी भी! खुशी से मदद करूँगा!"
        });
        
        RESPONSES.put("NAME_HI", new String[]{
            "मेरा नाम VoiceAI है! आपसे मिलकर अच्छा लगा! 🤖",
            "मुझे VoiceAI सहायक कहा जाता है। आपका नाम क्या है?",
            "मैं आपका मित्रवत AI वॉयस सहायक हूँ!"
        });
        
        RESPONSES.put("ABOUT_HI", new String[]{
            "मैं Java और Spring Boot के साथ बनाया गया AI/ML वॉयस सहायक हूँ! 🚀",
            "मैं VoiceAI हूँ - मशीन लर्निंग द्वारा संचालित आपका बुद्धिमान वॉयस सहायक।",
            "मैं मौसम, समय, संगीत, चुटकुले और बहुत कुछ में मदद कर सकता हूँ! बस पूछें!"
        });
        
        RESPONSES.put("UNKNOWN_HI", new String[]{
            "मुझे समझ नहीं आया। क्या आप फिर से कह सकते हैं?",
            "क्षमा करें, मैं वह नहीं समझ सका। क्या आप इसे अलग तरीके से कह सकते हैं?",
            "मैं अभी भी सीख रहा हूँ! क्या आप इसे अलग तरीके से पूछ सकते हैं?"
        });
        
        // English responses
        RESPONSES.put("GREETING_EN", new String[]{
            "Hello! How can I help you today? 😊",
            "Hi there! What can I do for you?",
            "Hey! Great to see you! How can I assist you?"
        });
        
        RESPONSES.put("TIME_EN", new String[]{
            "The current time is: " + new Date(),
            "It's " + new Date() + " right now.",
            "The time is " + new Date()
        });
        
        RESPONSES.put("WEATHER_EN", new String[]{
            "I'm checking the weather. It looks beautiful today! ☀️",
            "The forecast shows clear skies and pleasant weather.",
            "Weather looks great! Perfect day ahead."
        });
        
        RESPONSES.put("MUSIC_EN", new String[]{
            "🎵 Playing some great music for you!",
            "Let me find the perfect playlist for you! 🎶",
            "I'd love to play some music! Enjoy! 🎵"
        });
        
        RESPONSES.put("HELP_EN", new String[]{
            "I'm here to help! I can assist with weather, time, music, jokes, and more!",
            "What can I help you with today? Just ask me anything!",
            "I'm your personal assistant! I can handle various tasks for you."
        });
        
        RESPONSES.put("JOKE_EN", new String[]{
            "Why don't scientists trust atoms? Because they make up everything! 😂",
            "What do you call a bear with no teeth? A gummy bear! 🐻",
            "Why did the chicken cross the road? To get to the other side! 🐔"
        });
        
        RESPONSES.put("GOODBYE_EN", new String[]{
            "Goodbye! Have a wonderful day! 🌟",
            "See you later! Take care!",
            "Bye for now! Come back anytime!"
        });
        
        RESPONSES.put("THANKYOU_EN", new String[]{
            "You're welcome! I'm here to help anytime! 😊",
            "My pleasure! Let me know if you need anything else!",
            "Anytime! Happy to assist!"
        });
        
        RESPONSES.put("NAME_EN", new String[]{
            "My name is VoiceAI! Nice to meet you! 🤖",
            "I'm called VoiceAI Assistant. What's your name?",
            "I'm your friendly AI Voice Assistant!"
        });
        
        RESPONSES.put("ABOUT_EN", new String[]{
            "I'm an AI/ML Voice Assistant built with Java and Spring Boot! 🚀",
            "I'm VoiceAI - your intelligent voice assistant powered by machine learning.",
            "I can help with weather, time, music, jokes, and more! Just ask!"
        });
        
        RESPONSES.put("UNKNOWN_EN", new String[]{
            "I'm not sure I understand. Can you rephrase?",
            "Sorry, I didn't catch that. Could you say it differently?",
            "I'm still learning! Could you try asking in a different way?"
        });
    }
    
    public Map<String, Object> processMessage(String message, String sessionId, String language) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Check if message is Hindi (contains Devanagari characters)
            boolean isHindi = false;
            if (message != null && language != null && language.equals("hi")) {
                isHindi = true;
            }
            
            String intent = "UNKNOWN";
            String response = "";
            
            if (isHindi) {
                // Hindi detection
                intent = detectHindiIntent(message);
                response = getHindiResponse(intent);
            } else {
                // English detection
                intent = detectEnglishIntent(message);
                response = getEnglishResponse(intent);
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
            
            System.out.println("📝 Message: " + message + " | Intent: " + intent + " | Language: " + language);
            
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
    
    private String detectHindiIntent(String message) {
        if (message == null || message.trim().isEmpty()) {
            return "UNKNOWN";
        }
        
        String msg = message.trim();
        String bestIntent = "UNKNOWN";
        int maxScore = 0;
        
        for (Map.Entry<String, String[]> entry : HINDI_KEYWORDS.entrySet()) {
            String intent = entry.getKey();
            String[] keywords = entry.getValue();
            int score = 0;
            
            for (String keyword : keywords) {
                if (msg.contains(keyword)) {
                    score += 2;
                }
            }
            
            if (score > maxScore) {
                maxScore = score;
                bestIntent = intent;
            }
        }
        
        return maxScore > 0 ? bestIntent : "UNKNOWN";
    }
    
    private String detectEnglishIntent(String message) {
        if (message == null || message.trim().isEmpty()) {
            return "UNKNOWN";
        }
        
        String msg = message.toLowerCase().trim();
        
        if (msg.contains("hello") || msg.contains("hi") || msg.contains("hey")) {
            return "GREETING";
        } else if (msg.contains("time") || msg.contains("clock")) {
            return "TIME";
        } else if (msg.contains("weather") || msg.contains("rain") || msg.contains("sunny")) {
            return "WEATHER";
        } else if (msg.contains("music") || msg.contains("song") || msg.contains("play")) {
            return "MUSIC";
        } else if (msg.contains("help") || msg.contains("assist")) {
            return "HELP";
        } else if (msg.contains("joke") || msg.contains("funny") || msg.contains("laugh")) {
            return "JOKE";
        } else if (msg.contains("bye") || msg.contains("goodbye") || msg.contains("see you")) {
            return "GOODBYE";
        } else if (msg.contains("thank") || msg.contains("thanks")) {
            return "THANKYOU";
        } else if (msg.contains("name") || msg.contains("who are you")) {
            return "NAME";
        } else if (msg.contains("about") || msg.contains("what are you") || msg.contains("can you do")) {
            return "ABOUT";
        } else {
            return "UNKNOWN";
        }
    }
    
    private String getHindiResponse(String intent) {
        String key = intent + "_HI";
        String[] responses = RESPONSES.getOrDefault(key, RESPONSES.get("UNKNOWN_HI"));
        Random random = new Random();
        return responses[random.nextInt(responses.length)];
    }
    
    private String getEnglishResponse(String intent) {
        String key = intent + "_EN";
        String[] responses = RESPONSES.getOrDefault(key, RESPONSES.get("UNKNOWN_EN"));
        Random random = new Random();
        return responses[random.nextInt(responses.length)];
    }
    
    public List<Conversation> getHistory(String sessionId) {
        return conversationRepository.findBySessionIdOrderByCreatedAtDesc(sessionId);
    }
}
