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
    
    // Language-specific keyword maps
    private static final Map<String, Map<String, String[]>> LANGUAGE_KEYWORDS = new HashMap<>();
    private static final Map<String, Map<String, String[]>> LANGUAGE_RESPONSES = new HashMap<>();
    
    static {
        // ===== ENGLISH KEYWORDS =====
        Map<String, String[]> enKeywords = new HashMap<>();
        enKeywords.put("GREETING", new String[]{"hello", "hi", "hey", "greetings", "good morning", "good afternoon", "good evening", "how are you", "what's up"});
        enKeywords.put("TIME", new String[]{"time", "clock", "hour", "minute", "what time", "current time", "time now", "o'clock"});
        enKeywords.put("WEATHER", new String[]{"weather", "rain", "sunny", "cloud", "temperature", "forecast", "hot", "cold", "warm"});
        enKeywords.put("MUSIC", new String[]{"music", "song", "play", "listen", "melody", "tune", "playlist", "audio"});
        enKeywords.put("HELP", new String[]{"help", "assist", "support", "guide", "advice", "suggestion", "stuck", "problem"});
        enKeywords.put("JOKE", new String[]{"joke", "funny", "laugh", "humor", "comedy", "hilarious", "make me laugh"});
        enKeywords.put("GOODBYE", new String[]{"bye", "goodbye", "see you", "farewell", "later", "take care", "see ya"});
        enKeywords.put("THANKYOU", new String[]{"thank", "thanks", "appreciate", "grateful", "thank you", "thanks a lot"});
        enKeywords.put("NAME", new String[]{"name", "call you", "who are you", "your name", "what are you called"});
        enKeywords.put("ABOUT", new String[]{"about", "what are you", "tell me about yourself", "what can you do", "what do you do"});
        LANGUAGE_KEYWORDS.put("en", enKeywords);
        
        // ===== HINDI KEYWORDS =====
        Map<String, String[]> hiKeywords = new HashMap<>();
        hiKeywords.put("GREETING", new String[]{"नमस्ते", "नमस्कार", "हेलो", "हाय", "प्रणाम", "गुड मॉर्निंग", "शुभ प्रभात", "कैसे हो", "कैसे हैं", "आप कैसे हैं"});
        hiKeywords.put("TIME", new String[]{"समय", "घड़ी", "घंटा", "मिनट", "टाइम", "वक्त", "कितना बजा", "बजे", "समय क्या है", "क्या समय"});
        hiKeywords.put("WEATHER", new String[]{"मौसम", "बारिश", "धूप", "बादल", "तापमान", "गर्मी", "सर्दी", "ठंड", "मौसम कैसा", "कैसा मौसम"});
        hiKeywords.put("MUSIC", new String[]{"संगीत", "गाना", "बजाओ", "सुनो", "प्लेलिस्ट", "म्यूजिक", "गीत", "संगीत बजाओ", "गाना बजाओ"});
        hiKeywords.put("HELP", new String[]{"मदद", "सहायता", "सपोर्ट", "सलाह", "समस्या", "मुश्किल", "मदद करो", "मेरी मदद करो"});
        hiKeywords.put("JOKE", new String[]{"मजाक", "चुटकुला", "हँसी", "हास्य", "मुझे हँसाओ", "मजेदार", "मजाक सुनाओ", "चुटकुला सुनाओ"});
        hiKeywords.put("GOODBYE", new String[]{"अलविदा", "फिर मिलेंगे", "बाय", "गुडबाय", "ध्यान रखना", "जल्द ही मिलेंगे"});
        hiKeywords.put("THANKYOU", new String[]{"धन्यवाद", "शुक्रिया", "थैंक्स", "आभार", "बहुत धन्यवाद"});
        hiKeywords.put("NAME", new String[]{"नाम", "आपका नाम", "क्या नाम है", "कौन हो", "आप कौन हैं", "नाम बताओ"});
        hiKeywords.put("ABOUT", new String[]{"क्या हो", "बारे में", "अपने बारे में बताओ", "क्या कर सकते हो", "तुम क्या हो"});
        LANGUAGE_KEYWORDS.put("hi", hiKeywords);
        
        // ===== SPANISH KEYWORDS =====
        Map<String, String[]> esKeywords = new HashMap<>();
        esKeywords.put("GREETING", new String[]{"hola", "buenos días", "buenas tardes", "buenas noches", "cómo estás", "saludos", "qué tal"});
        esKeywords.put("TIME", new String[]{"hora", "reloj", "qué hora", "hora actual", "tiempo", "hora es"});
        esKeywords.put("WEATHER", new String[]{"clima", "lluvia", "soleado", "nube", "temperatura", "calor", "frío", "tiempo"});
        esKeywords.put("MUSIC", new String[]{"música", "canción", "reproducir", "escuchar", "playlist", "musica"});
        esKeywords.put("HELP", new String[]{"ayuda", "asistencia", "apoyo", "guía", "consejo", "problema", "ayudar"});
        esKeywords.put("JOKE", new String[]{"broma", "chiste", "risa", "humor", "gracioso", "divertido"});
        esKeywords.put("GOODBYE", new String[]{"adiós", "hasta luego", "hasta la vista", "cuídate", "nos vemos", "chao"});
        esKeywords.put("THANKYOU", new String[]{"gracias", "muchas gracias", "agradecer"});
        esKeywords.put("NAME", new String[]{"nombre", "cómo te llamas", "quién eres"});
        esKeywords.put("ABOUT", new String[]{"sobre", "qué eres", "qué puedes hacer", "cuéntame sobre ti"});
        LANGUAGE_KEYWORDS.put("es", esKeywords);
        
        // ===== FRENCH KEYWORDS =====
        Map<String, String[]> frKeywords = new HashMap<>();
        frKeywords.put("GREETING", new String[]{"bonjour", "salut", "coucou", "bonsoir", "comment ça va", "ça va"});
        frKeywords.put("TIME", new String[]{"heure", "quelle heure", "horaire", "minute", "temps"});
        frKeywords.put("WEATHER", new String[]{"météo", "pluie", "soleil", "nuage", "température", "chaud", "froid"});
        frKeywords.put("MUSIC", new String[]{"musique", "chanson", "jouer", "écouter", "playlist", "mélodie"});
        frKeywords.put("HELP", new String[]{"aide", "assistance", "soutien", "guide", "conseil", "problème"});
        frKeywords.put("JOKE", new String[]{"blague", "rire", "humour", "drôle", "comédie"});
        frKeywords.put("GOODBYE", new String[]{"au revoir", "salut", "à plus", "adieu", "à bientôt", "bye"});
        frKeywords.put("THANKYOU", new String[]{"merci", "merci beaucoup", "remarcier"});
        frKeywords.put("NAME", new String[]{"nom", "comment tu t'appelles", "qui es-tu"});
        frKeywords.put("ABOUT", new String[]{"sur", "qui es-tu", "que fais-tu", "parle-moi de toi"});
        LANGUAGE_KEYWORDS.put("fr", frKeywords);
        
        // ===== GERMAN KEYWORDS =====
        Map<String, String[]> deKeywords = new HashMap<>();
        deKeywords.put("GREETING", new String[]{"hallo", "hi", "guten morgen", "guten tag", "guten abend", "wie geht es dir"});
        deKeywords.put("TIME", new String[]{"uhr", "zeit", "wie spät", "stunde", "minute"});
        deKeywords.put("WEATHER", new String[]{"wetter", "regen", "sonne", "wolke", "temperatur", "heiß", "kalt"});
        deKeywords.put("MUSIC", new String[]{"musik", "lied", "spielen", "hören", "playlist", "melodie"});
        deKeywords.put("HELP", new String[]{"hilfe", "unterstützung", "rat", "problem", "assistenz"});
        deKeywords.put("JOKE", new String[]{"witz", "lachen", "humor", "komisch", "spaß"});
        deKeywords.put("GOODBYE", new String[]{"auf wiedersehen", "tschüss", "bis bald", "ade", "bye"});
        deKeywords.put("THANKYOU", new String[]{"danke", "danke schön", "vielen dank"});
        deKeywords.put("NAME", new String[]{"name", "wie heißt du", "wer bist du"});
        deKeywords.put("ABOUT", new String[]{"über", "wer bist du", "was machst du", "erzähl mir von dir"});
        LANGUAGE_KEYWORDS.put("de", deKeywords);
        
        // ===== JAPANESE KEYWORDS =====
        Map<String, String[]> jaKeywords = new HashMap<>();
        jaKeywords.put("GREETING", new String[]{"こんにちは", "こんばんは", "おはよう", "やあ", "どうも", "お元気ですか"});
        jaKeywords.put("TIME", new String[]{"時間", "今何時", "時刻", "時"});
        jaKeywords.put("WEATHER", new String[]{"天気", "雨", "晴れ", "雲", "気温", "暑い", "寒い"});
        jaKeywords.put("MUSIC", new String[]{"音楽", "曲", "再生", "聴く", "プレイリスト"});
        jaKeywords.put("HELP", new String[]{"助け", "支援", "ガイド", "アドバイス", "問題"});
        jaKeywords.put("JOKE", new String[]{"ジョーク", "笑い", "ユーモア", "面白い"});
        jaKeywords.put("GOODBYE", new String[]{"さようなら", "じゃあ", "またね", "バイバイ"});
        jaKeywords.put("THANKYOU", new String[]{"ありがとう", "ありがとうございます"});
        jaKeywords.put("NAME", new String[]{"名前", "お名前", "何という名前"});
        jaKeywords.put("ABOUT", new String[]{"について", "誰", "何ができる"});
        LANGUAGE_KEYWORDS.put("ja", jaKeywords);
        
        // ===== CHINESE KEYWORDS =====
        Map<String, String[]> zhKeywords = new HashMap<>();
        zhKeywords.put("GREETING", new String[]{"你好", "您好", "嗨", "早上好", "下午好", "晚上好", "你好吗"});
        zhKeywords.put("TIME", new String[]{"时间", "几点", "小时", "分钟"});
        zhKeywords.put("WEATHER", new String[]{"天气", "雨", "晴", "云", "温度", "热", "冷"});
        zhKeywords.put("MUSIC", new String[]{"音乐", "歌曲", "播放", "听", "播放列表"});
        zhKeywords.put("HELP", new String[]{"帮助", "支援", "指导", "建议", "问题"});
        zhKeywords.put("JOKE", new String[]{"笑话", "笑", "幽默", "有趣"});
        zhKeywords.put("GOODBYE", new String[]{"再见", "拜拜", "回头见"});
        zhKeywords.put("THANKYOU", new String[]{"谢谢", "非常感谢"});
        zhKeywords.put("NAME", new String[]{"名字", "姓名", "叫什么"});
        zhKeywords.put("ABOUT", new String[]{"关于", "是谁", "能做什么"});
        LANGUAGE_KEYWORDS.put("zh", zhKeywords);
    }
    
    public Map<String, Object> processMessage(String message, String sessionId, String language) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Detect intent based on language
            String intent = detectIntent(message, language);
            String response = getResponseForIntent(intent, language);
            
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
    
    private String detectIntent(String message, String language) {
        if (message == null || message.trim().isEmpty()) {
            return "UNKNOWN";
        }
        
        String msg = message.toLowerCase().trim();
        Map<String, Integer> scores = new HashMap<>();
        
        // Get keywords for the language, fallback to English
        Map<String, String[]> keywords = LANGUAGE_KEYWORDS.getOrDefault(language, LANGUAGE_KEYWORDS.get("en"));
        
        for (Map.Entry<String, String[]> entry : keywords.entrySet()) {
            String intent = entry.getKey();
            String[] words = entry.getValue();
            int score = 0;
            
            for (String word : words) {
                if (msg.contains(word.toLowerCase())) {
                    score += 2;
                }
            }
            
            if (score > 0) {
                scores.put(intent, score);
            }
        }
        
        String bestIntent = "UNKNOWN";
        int maxScore = 0;
        
        for (Map.Entry<String, Integer> entry : scores.entrySet()) {
            if (entry.getValue() > maxScore) {
                maxScore = entry.getValue();
                bestIntent = entry.getKey();
            }
        }
        
        return maxScore > 0 ? bestIntent : "UNKNOWN";
    }
    
    private String getResponseForIntent(String intent, String language) {
        // For now, use English responses for all languages
        // In a full implementation, you'd have language-specific responses
        Map<String, String[]> responses = new HashMap<>();
        
        responses.put("GREETING", new String[]{
            "Hello! How can I help you today? 😊",
            "Hi there! What can I do for you?",
            "Hey! Great to see you! How can I assist you?"
        });
        responses.put("TIME", new String[]{
            "The current time is: " + new Date(),
            "It's " + new Date() + " right now.",
            "The time is " + new Date()
        });
        responses.put("WEATHER", new String[]{
            "I'm checking the weather. It looks beautiful today! ☀️",
            "The forecast shows clear skies and pleasant weather.",
            "Weather looks great! Perfect day ahead."
        });
        responses.put("MUSIC", new String[]{
            "🎵 Playing some great music for you!",
            "Let me find the perfect playlist for you! 🎶",
            "I'd love to play some music! Enjoy! 🎵"
        });
        responses.put("HELP", new String[]{
            "I'm here to help! I can assist with weather, time, music, jokes, and more!",
            "What can I help you with today? Just ask me anything!",
            "I'm your personal assistant! I can handle various tasks for you."
        });
        responses.put("JOKE", new String[]{
            "Why don't scientists trust atoms? Because they make up everything! 😂",
            "What do you call a bear with no teeth? A gummy bear! 🐻",
            "Why did the chicken cross the road? To get to the other side! 🐔"
        });
        responses.put("GOODBYE", new String[]{
            "Goodbye! Have a wonderful day! 🌟",
            "See you later! Take care!",
            "Bye for now! Come back anytime!"
        });
        responses.put("THANKYOU", new String[]{
            "You're welcome! I'm here to help anytime! 😊",
            "My pleasure! Let me know if you need anything else!",
            "Anytime! Happy to assist!"
        });
        responses.put("NAME", new String[]{
            "My name is VoiceAI! Nice to meet you! 🤖",
            "I'm called VoiceAI Assistant. What's your name?",
            "I'm your friendly AI Voice Assistant!"
        });
        responses.put("ABOUT", new String[]{
            "I'm an AI/ML Voice Assistant built with Java and Spring Boot! 🚀",
            "I'm VoiceAI - your intelligent voice assistant powered by machine learning.",
            "I can help with weather, time, music, jokes, and more! Just ask!"
        });
        responses.put("UNKNOWN", new String[]{
            "I'm not sure I understand. Can you rephrase?",
            "Sorry, I didn't catch that. Could you say it differently?",
            "I'm still learning! Could you try asking in a different way?"
        });
        
        String[] responseList = responses.getOrDefault(intent, responses.get("UNKNOWN"));
        Random random = new Random();
        return responseList[random.nextInt(responseList.length)];
    }
    
    public List<Conversation> getHistory(String sessionId) {
        return conversationRepository.findBySessionIdOrderByCreatedAtDesc(sessionId);
    }
}
