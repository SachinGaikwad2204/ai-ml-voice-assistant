package com.aiml.voice.service;

import com.aiml.voice.model.Conversation;
import com.aiml.voice.repository.ConversationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.text.Normalizer;
import java.util.*;

@Service
public class AIService {

    @Autowired
    private ConversationRepository conversationRepository;
    // Removed unused IntentClassifierService dependency — this class is the
    // single source of truth for intent detection. If you want to bring in
    // the neural classifier later, merge it explicitly and deliberately.

    private static final Map<String, Map<String, String[]>> LANGUAGE_KEYWORDS = new HashMap<>();
    private static final Map<String, Map<String, String[]>> LANGUAGE_RESPONSES = new HashMap<>();

    static {
        // ===== ENGLISH =====
        Map<String, String[]> enKeywords = new HashMap<>();
        enKeywords.put("GREETING", new String[]{"hello", "hi", "hey", "greetings", "good morning", "good afternoon", "good evening", "how are you", "what's up"});
        enKeywords.put("TIME", new String[]{"time", "clock", "hour", "minute", "what time", "current time", "time now"});
        enKeywords.put("WEATHER", new String[]{"weather", "rain", "sunny", "cloud", "temperature", "forecast", "hot", "cold"});
        enKeywords.put("MUSIC", new String[]{"music", "song", "play", "listen", "melody", "tune", "playlist"});
        enKeywords.put("HELP", new String[]{"help", "assist", "support", "guide", "advice", "suggestion", "stuck", "problem"});
        enKeywords.put("JOKE", new String[]{"joke", "funny", "laugh", "humor", "comedy", "hilarious"});
        enKeywords.put("GOODBYE", new String[]{"bye", "goodbye", "see you", "farewell", "later", "take care"});
        enKeywords.put("THANKYOU", new String[]{"thank", "thanks", "appreciate", "grateful", "thank you"});
        enKeywords.put("NAME", new String[]{"name", "call you", "who are you", "your name"});
        enKeywords.put("ABOUT", new String[]{"about", "what are you", "tell me about yourself", "what can you do"});
        LANGUAGE_KEYWORDS.put("en", enKeywords);

        // ===== GERMAN =====
        Map<String, String[]> deKeywords = new HashMap<>();
        deKeywords.put("GREETING", new String[]{
            "hallo", "halo", "hello",
            "guten morgen", "gutenmorgen", "morgen",
            "guten tag", "gutentag", "tag",
            "guten abend", "gutenabend", "abend",
            "wie geht es dir", "wie gehts", "wie geht's", "wie geht es ihnen",
            "servus", "moin", "hi"
        });
        deKeywords.put("TIME", new String[]{"uhr", "zeit", "wie spät", "spät", "stunde", "minute", "uhrzeit"});
        deKeywords.put("WEATHER", new String[]{"wetter", "regen", "sonne", "wolke", "temperatur", "heiß", "kalt", "warm", "schnee"});
        deKeywords.put("MUSIC", new String[]{"musik", "lied", "spielen", "hören", "playlist", "melodie", "song"});
        deKeywords.put("HELP", new String[]{"hilfe", "unterstützung", "rat", "problem", "assistenz", "helfen"});
        deKeywords.put("JOKE", new String[]{"witz", "lachen", "humor", "komisch", "spaß", "witze"});
        deKeywords.put("GOODBYE", new String[]{"auf wiedersehen", "wiedersehen", "tschüss", "bis bald", "ade", "bye", "tschüssi"});
        deKeywords.put("THANKYOU", new String[]{"danke", "danke schön", "dankeschön", "vielen dank"});
        deKeywords.put("NAME", new String[]{"name", "wie heißt du", "wie heisst du", "wer bist du", "dein name"});
        deKeywords.put("ABOUT", new String[]{"über", "was machst du", "erzähl mir von dir", "was kannst du"});
        LANGUAGE_KEYWORDS.put("de", deKeywords);

        // ===== HINDI =====
        Map<String, String[]> hiKeywords = new HashMap<>();
        hiKeywords.put("GREETING", new String[]{"नमस्ते", "नमस्कार", "हेलो", "हाय", "प्रणाम", "गुड मॉर्निंग", "शुभ प्रभात", "कैसे हो", "कैसे हैं", "आप कैसे हैं"});
        hiKeywords.put("TIME", new String[]{"समय", "घड़ी", "घंटा", "मिनट", "टाइम", "वक्त", "कितना बजा", "बजे"});
        hiKeywords.put("WEATHER", new String[]{"मौसम", "बारिश", "धूप", "बादल", "तापमान", "गर्मी", "सर्दी", "ठंड"});
        hiKeywords.put("MUSIC", new String[]{"संगीत", "गाना", "बजाओ", "सुनो", "प्लेलिस्ट", "म्यूजिक", "गीत"});
        hiKeywords.put("HELP", new String[]{"मदद", "सहायता", "सपोर्ट", "सलाह", "समस्या", "मुश्किल"});
        hiKeywords.put("JOKE", new String[]{"मजाक", "चुटकुला", "हँसी", "हास्य", "मजेदार"});
        hiKeywords.put("GOODBYE", new String[]{"अलविदा", "फिर मिलेंगे", "बाय", "गुडबाय", "ध्यान रखना"});
        hiKeywords.put("THANKYOU", new String[]{"धन्यवाद", "शुक्रिया", "थैंक्स", "आभार"});
        hiKeywords.put("NAME", new String[]{"नाम", "आपका नाम", "क्या नाम है", "कौन हो"});
        hiKeywords.put("ABOUT", new String[]{"बारे में", "अपने बारे में बताओ", "क्या कर सकते हो"});
        LANGUAGE_KEYWORDS.put("hi", hiKeywords);

        // ===== SPANISH =====
        Map<String, String[]> esKeywords = new HashMap<>();
        esKeywords.put("GREETING", new String[]{"hola", "buenos días", "buenas tardes", "buenas noches", "cómo estás", "saludos", "qué tal"});
        esKeywords.put("TIME", new String[]{"hora", "reloj", "qué hora", "hora actual"});
        esKeywords.put("WEATHER", new String[]{"clima", "lluvia", "soleado", "nube", "temperatura", "calor", "frío"});
        esKeywords.put("MUSIC", new String[]{"música", "canción", "reproducir", "escuchar", "playlist"});
        esKeywords.put("HELP", new String[]{"ayuda", "asistencia", "apoyo", "guía", "consejo", "problema"});
        esKeywords.put("JOKE", new String[]{"broma", "chiste", "risa", "humor", "gracioso", "divertido"});
        esKeywords.put("GOODBYE", new String[]{"adiós", "hasta luego", "hasta la vista", "cuídate", "nos vemos", "chao"});
        esKeywords.put("THANKYOU", new String[]{"gracias", "muchas gracias", "agradecer"});
        esKeywords.put("NAME", new String[]{"nombre", "cómo te llamas", "quién eres"});
        esKeywords.put("ABOUT", new String[]{"qué eres", "qué puedes hacer", "cuéntame sobre ti"});
        LANGUAGE_KEYWORDS.put("es", esKeywords);

        // ===== FRENCH =====
        Map<String, String[]> frKeywords = new HashMap<>();
        frKeywords.put("GREETING", new String[]{"bonjour", "salut", "coucou", "bonsoir", "comment ça va", "ça va"});
        frKeywords.put("TIME", new String[]{"heure", "quelle heure", "horaire"});
        frKeywords.put("WEATHER", new String[]{"météo", "pluie", "soleil", "nuage", "température", "chaud", "froid"});
        frKeywords.put("MUSIC", new String[]{"musique", "chanson", "jouer", "écouter", "playlist"});
        frKeywords.put("HELP", new String[]{"aide", "assistance", "soutien", "guide", "conseil", "problème"});
        frKeywords.put("JOKE", new String[]{"blague", "rire", "humour", "drôle"});
        frKeywords.put("GOODBYE", new String[]{"au revoir", "à plus", "adieu", "à bientôt"});
        frKeywords.put("THANKYOU", new String[]{"merci", "merci beaucoup"});
        frKeywords.put("NAME", new String[]{"nom", "comment tu t'appelles", "qui es-tu"});
        frKeywords.put("ABOUT", new String[]{"que fais-tu", "parle-moi de toi"});
        LANGUAGE_KEYWORDS.put("fr", frKeywords);

        // ===== JAPANESE =====
        Map<String, String[]> jaKeywords = new HashMap<>();
        jaKeywords.put("GREETING", new String[]{"こんにちは", "こんばんは", "おはよう", "やあ", "どうも", "お元気ですか"});
        jaKeywords.put("TIME", new String[]{"時間", "今何時", "時刻"});
        jaKeywords.put("WEATHER", new String[]{"天気", "雨", "晴れ", "気温", "暑い", "寒い"});
        jaKeywords.put("MUSIC", new String[]{"音楽", "曲", "再生", "聴く", "プレイリスト"});
        jaKeywords.put("HELP", new String[]{"助け", "支援", "アドバイス", "問題"});
        jaKeywords.put("JOKE", new String[]{"ジョーク", "笑い", "ユーモア", "面白い"});
        jaKeywords.put("GOODBYE", new String[]{"さようなら", "またね", "バイバイ"});
        jaKeywords.put("THANKYOU", new String[]{"ありがとう", "ありがとうございます"});
        jaKeywords.put("NAME", new String[]{"名前", "お名前"});
        jaKeywords.put("ABOUT", new String[]{"について", "何ができる"});
        LANGUAGE_KEYWORDS.put("ja", jaKeywords);

        // ===== CHINESE =====
        Map<String, String[]> zhKeywords = new HashMap<>();
        zhKeywords.put("GREETING", new String[]{"你好", "您好", "嗨", "早上好", "下午好", "晚上好"});
        zhKeywords.put("TIME", new String[]{"时间", "几点", "小时", "分钟"});
        zhKeywords.put("WEATHER", new String[]{"天气", "下雨", "晴", "温度", "热", "冷"});
        zhKeywords.put("MUSIC", new String[]{"音乐", "歌曲", "播放", "播放列表"});
        zhKeywords.put("HELP", new String[]{"帮助", "支援", "指导", "建议"});
        zhKeywords.put("JOKE", new String[]{"笑话", "幽默", "有趣"});
        zhKeywords.put("GOODBYE", new String[]{"再见", "拜拜", "回头见"});
        zhKeywords.put("THANKYOU", new String[]{"谢谢", "非常感谢"});
        zhKeywords.put("NAME", new String[]{"名字", "姓名"});
        zhKeywords.put("ABOUT", new String[]{"关于", "能做什么"});
        LANGUAGE_KEYWORDS.put("zh", zhKeywords);

        // ===== RESPONSES =====
        Map<String, String[]> enResponses = new HashMap<>();
        enResponses.put("GREETING", new String[]{"Hello! How can I help you today? 😊", "Hi there! What can I do for you?"});
        enResponses.put("TIME", new String[]{"The current time is: " + new Date()});
        enResponses.put("WEATHER", new String[]{"It looks beautiful today! ☀️"});
        enResponses.put("MUSIC", new String[]{"🎵 Playing some great music for you!"});
        enResponses.put("HELP", new String[]{"I'm here to help! I can assist with weather, time, music, jokes, and more!"});
        enResponses.put("JOKE", new String[]{"Why don't scientists trust atoms? Because they make up everything! 😂"});
        enResponses.put("GOODBYE", new String[]{"Goodbye! Have a wonderful day! 🌟"});
        enResponses.put("THANKYOU", new String[]{"You're welcome! I'm here to help anytime! 😊"});
        enResponses.put("NAME", new String[]{"My name is VoiceAI! Nice to meet you! 🤖"});
        enResponses.put("ABOUT", new String[]{"I'm an AI/ML Voice Assistant built with Java and Spring Boot! 🚀"});
        enResponses.put("UNKNOWN", new String[]{"I'm not sure I understand. Can you rephrase?", "Sorry, I didn't catch that."});
        LANGUAGE_RESPONSES.put("en", enResponses);

        Map<String, String[]> deResponses = new HashMap<>();
        deResponses.put("GREETING", new String[]{"Hallo! Wie kann ich Ihnen helfen? 😊", "Guten Tag! Wie kann ich Ihnen helfen?"});
        deResponses.put("TIME", new String[]{"Die aktuelle Zeit ist: " + new Date()});
        deResponses.put("WEATHER", new String[]{"Das Wetter ist heute schön! ☀️"});
        deResponses.put("MUSIC", new String[]{"🎵 Ich spiele Musik für Sie!"});
        deResponses.put("HELP", new String[]{"Ich kann bei Wetter, Zeit, Musik, Witzen und mehr helfen!"});
        deResponses.put("JOKE", new String[]{"Warum vertrauen Wissenschaftler Atomen nicht? Weil sie alles erfinden! 😂"});
        deResponses.put("GOODBYE", new String[]{"Auf Wiedersehen! 🌟"});
        deResponses.put("THANKYOU", new String[]{"Gern geschehen! 😊"});
        deResponses.put("NAME", new String[]{"Ich heiße VoiceAI! 🤖"});
        deResponses.put("ABOUT", new String[]{"Ich bin ein AI/ML Sprachassistent! 🚀"});
        deResponses.put("UNKNOWN", new String[]{"Ich verstehe nicht. Können Sie es umformulieren?", "Entschuldigung, ich habe das nicht verstanden."});
        LANGUAGE_RESPONSES.put("de", deResponses);

        Map<String, String[]> hiResponses = new HashMap<>();
        hiResponses.put("GREETING", new String[]{"नमस्ते! मैं आपकी कैसे मदद कर सकता हूँ? 😊"});
        hiResponses.put("TIME", new String[]{"वर्तमान समय है: " + new Date()});
        hiResponses.put("WEATHER", new String[]{"आज का मौसम बहुत सुंदर है! ☀️"});
        hiResponses.put("MUSIC", new String[]{"🎵 आपके लिए संगीत बजा रहा हूँ!"});
        hiResponses.put("HELP", new String[]{"मैं मौसम, समय, संगीत, चुटकुले में मदद कर सकता हूँ!"});
        hiResponses.put("JOKE", new String[]{"वैज्ञानिक परमाणुओं पर भरोसा क्यों नहीं करते? क्योंकि वे सब कुछ बनाते हैं! 😂"});
        hiResponses.put("GOODBYE", new String[]{"अलविदा! आपका दिन शुभ हो! 🌟"});
        hiResponses.put("THANKYOU", new String[]{"आपका स्वागत है! 😊"});
        hiResponses.put("NAME", new String[]{"मेरा नाम VoiceAI है! 🤖"});
        hiResponses.put("ABOUT", new String[]{"मैं AI/ML वॉयस सहायक हूँ! 🚀"});
        hiResponses.put("UNKNOWN", new String[]{"मुझे समझ नहीं आया। क्या आप फिर से कह सकते हैं?"});
        LANGUAGE_RESPONSES.put("hi", hiResponses);

        Map<String, String[]> esResponses = new HashMap<>();
        esResponses.put("GREETING", new String[]{"¡Hola! ¿Cómo puedo ayudarte? 😊"});
        esResponses.put("TIME", new String[]{"La hora actual es: " + new Date()});
        esResponses.put("WEATHER", new String[]{"¡El clima se ve hermoso hoy! ☀️"});
        esResponses.put("MUSIC", new String[]{"🎵 ¡Reproduciendo música para ti!"});
        esResponses.put("HELP", new String[]{"¡Puedo ayudar con clima, hora, música, chistes y más!"});
        esResponses.put("JOKE", new String[]{"¿Por qué los científicos no confían en los átomos? ¡Porque lo inventan todo! 😂"});
        esResponses.put("GOODBYE", new String[]{"¡Adiós! ¡Que tengas un buen día! 🌟"});
        esResponses.put("THANKYOU", new String[]{"¡De nada! 😊"});
        esResponses.put("NAME", new String[]{"¡Mi nombre es VoiceAI! 🤖"});
        esResponses.put("ABOUT", new String[]{"¡Soy un asistente de voz AI/ML! 🚀"});
        esResponses.put("UNKNOWN", new String[]{"No entiendo. ¿Puedes reformular?"});
        LANGUAGE_RESPONSES.put("es", esResponses);

        Map<String, String[]> frResponses = new HashMap<>();
        frResponses.put("GREETING", new String[]{"Bonjour! Comment puis-je vous aider? 😊"});
        frResponses.put("TIME", new String[]{"L'heure actuelle est: " + new Date()});
        frResponses.put("WEATHER", new String[]{"Il fait beau aujourd'hui! ☀️"});
        frResponses.put("MUSIC", new String[]{"🎵 Je joue de la musique pour vous!"});
        frResponses.put("HELP", new String[]{"Je peux aider avec la météo, l'heure, la musique, les blagues!"});
        frResponses.put("JOKE", new String[]{"Pourquoi les scientifiques ne font-ils pas confiance aux atomes? Ils inventent tout! 😂"});
        frResponses.put("GOODBYE", new String[]{"Au revoir! 🌟"});
        frResponses.put("THANKYOU", new String[]{"Je vous en prie! 😊"});
        frResponses.put("NAME", new String[]{"Je m'appelle VoiceAI! 🤖"});
        frResponses.put("ABOUT", new String[]{"Je suis un assistant vocal AI/ML! 🚀"});
        frResponses.put("UNKNOWN", new String[]{"Je ne comprends pas. Pouvez-vous reformuler?"});
        LANGUAGE_RESPONSES.put("fr", frResponses);

        Map<String, String[]> jaResponses = new HashMap<>();
        jaResponses.put("GREETING", new String[]{"こんにちは！お手伝いできますか？😊"});
        jaResponses.put("TIME", new String[]{"現在の時刻は: " + new Date()});
        jaResponses.put("WEATHER", new String[]{"今日は良い天気です！☀️"});
        jaResponses.put("MUSIC", new String[]{"🎵 音楽を再生しています！"});
        jaResponses.put("HELP", new String[]{"天気、時間、音楽、ジョークについてお手伝いできます！"});
        jaResponses.put("JOKE", new String[]{"科学者はなぜ原子を信じないのか？すべてを作り上げるからです！😂"});
        jaResponses.put("GOODBYE", new String[]{"さようなら！🌟"});
        jaResponses.put("THANKYOU", new String[]{"どういたしまして！😊"});
        jaResponses.put("NAME", new String[]{"私の名前はVoiceAIです！🤖"});
        jaResponses.put("ABOUT", new String[]{"私はAI/ML音声アシスタントです！🚀"});
        jaResponses.put("UNKNOWN", new String[]{"理解できませんでした。言い換えていただけますか？"});
        LANGUAGE_RESPONSES.put("ja", jaResponses);

        Map<String, String[]> zhResponses = new HashMap<>();
        zhResponses.put("GREETING", new String[]{"你好！我能帮你什么？😊"});
        zhResponses.put("TIME", new String[]{"当前时间是: " + new Date()});
        zhResponses.put("WEATHER", new String[]{"今天天气很好！☀️"});
        zhResponses.put("MUSIC", new String[]{"🎵 正在播放音乐！"});
        zhResponses.put("HELP", new String[]{"我可以帮你处理天气、时间、音乐、笑话！"});
        zhResponses.put("JOKE", new String[]{"科学家为什么不信任原子？它们构成一切！😂"});
        zhResponses.put("GOODBYE", new String[]{"再见！🌟"});
        zhResponses.put("THANKYOU", new String[]{"不客气！😊"});
        zhResponses.put("NAME", new String[]{"我叫VoiceAI！🤖"});
        zhResponses.put("ABOUT", new String[]{"我是AI/ML语音助手！🚀"});
        zhResponses.put("UNKNOWN", new String[]{"我不太明白。你能换种说法吗？"});
        LANGUAGE_RESPONSES.put("zh", zhResponses);
    }

    public Map<String, Object> processMessage(String message, String sessionId, String language) {
        Map<String, Object> result = new HashMap<>();
        try {
            String intent = detectIntent(message, language);
            String response = getResponseForIntent(intent, language);

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

            // If this line is NOT in your Render logs, AIService is not being called.
            System.out.println("📝 [AIService] Message: " + message + " | Intent: " + intent + " | Language: " + language);

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
        if (message == null || message.trim().isEmpty()) return "UNKNOWN";

        String langKey = (language == null || language.trim().isEmpty()) ? "en" : language.trim().toLowerCase();
        String msg = normalize(message);

        Map<String, String[]> keywords = LANGUAGE_KEYWORDS.getOrDefault(langKey, LANGUAGE_KEYWORDS.get("en"));

        String bestIntent = "UNKNOWN";
        int maxScore = 0;

        for (Map.Entry<String, String[]> entry : keywords.entrySet()) {
            int score = 0;
            for (String word : entry.getValue()) {
                if (msg.contains(normalize(word))) {
                    // Longer/multi-word phrases are more specific signals — weight them higher.
                    score += word.trim().contains(" ") ? 3 : 2;
                }
            }
            if (score > maxScore) {
                maxScore = score;
                bestIntent = entry.getKey();
            }
        }
        return bestIntent;
    }

    /** Lowercases and strips punctuation so "Hallo!" / "hallo" / "HALLO?" all match the same way. */
    private String normalize(String s) {
        String lower = s.toLowerCase().trim();
        // Keep letters (incl. non-Latin scripts), digits, and spaces; drop punctuation like ! ? , .
        return lower.replaceAll("[!?,.;:]", " ").replaceAll("\\s+", " ").trim();
    }

    private String getResponseForIntent(String intent, String language) {
        String langKey = (language == null || language.trim().isEmpty()) ? "en" : language.trim().toLowerCase();
        Map<String, String[]> responses = LANGUAGE_RESPONSES.getOrDefault(langKey, LANGUAGE_RESPONSES.get("en"));
        String[] responseList = responses.getOrDefault(intent, responses.get("UNKNOWN"));
        return responseList[new Random().nextInt(responseList.length)];
    }

    public List<Conversation> getHistory(String sessionId) {
        return conversationRepository.findBySessionIdOrderByCreatedAtDesc(sessionId);
    }
}