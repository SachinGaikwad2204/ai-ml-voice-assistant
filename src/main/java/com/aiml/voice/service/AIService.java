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
    
    private static final Map<String, Map<String, String[]>> LANGUAGE_KEYWORDS = new HashMap<>();
    private static final Map<String, Map<String, String[]>> LANGUAGE_RESPONSES = new HashMap<>();
    
    static {
        // ===== ENGLISH KEYWORDS =====
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
        
        // ===== GERMAN KEYWORDS (Comprehensive) =====
        Map<String, String[]> deKeywords = new HashMap<>();
        deKeywords.put("GREETING", new String[]{
            "hallo", "halo", "hello", 
            "guten morgen", "gutenmorgen", "morgen", 
            "guten tag", "gutentag", "tag", 
            "guten abend", "gutenabend", "abend",
            "wie geht es dir", "wie gehts", "wie geht's", "wie geht es ihnen",
            "servus", "moin", "na", "hallo!", "hi"
        });
        deKeywords.put("TIME", new String[]{"uhr", "zeit", "wie spät", "spät", "stunde", "minute", "uhrzeit"});
        deKeywords.put("WEATHER", new String[]{"wetter", "regen", "sonne", "wolke", "temperatur", "heiß", "kalt", "warm", "schnee"});
        deKeywords.put("MUSIC", new String[]{"musik", "lied", "spielen", "hören", "playlist", "melodie", "song"});
        deKeywords.put("HELP", new String[]{"hilfe", "unterstützung", "rat", "problem", "assistenz", "helfen"});
        deKeywords.put("JOKE", new String[]{"witz", "lachen", "humor", "komisch", "spaß", "witze"});
        deKeywords.put("GOODBYE", new String[]{"auf wiedersehen", "wiedersehen", "tschüss", "bis bald", "ade", "bye", "tschüssi"});
        deKeywords.put("THANKYOU", new String[]{"danke", "danke schön", "dankeschön", "vielen dank", "merci"});
        deKeywords.put("NAME", new String[]{"name", "wie heißt du", "wie heisst du", "wer bist du", "dein name"});
        deKeywords.put("ABOUT", new String[]{"über", "wer bist du", "was machst du", "erzähl mir von dir", "was kannst du"});
        LANGUAGE_KEYWORDS.put("de", deKeywords);
        
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
        
        // ===== RESPONSES =====
        // English Responses
        Map<String, String[]> enResponses = new HashMap<>();
        enResponses.put("GREETING", new String[]{"Hello! How can I help you today? 😊", "Hi there! What can I do for you?"});
        enResponses.put("TIME", new String[]{"The current time is: " + new Date(), "It's " + new Date() + " right now."});
        enResponses.put("WEATHER", new String[]{"I'm checking the weather. It looks beautiful today! ☀️", "The forecast shows clear skies and pleasant weather."});
        enResponses.put("MUSIC", new String[]{"🎵 Playing some great music for you!", "Let me find the perfect playlist for you! 🎶"});
        enResponses.put("HELP", new String[]{"I'm here to help! I can assist with weather, time, music, jokes, and more!", "What can I help you with today?"});
        enResponses.put("JOKE", new String[]{"Why don't scientists trust atoms? Because they make up everything! 😂", "What do you call a bear with no teeth? A gummy bear! 🐻"});
        enResponses.put("GOODBYE", new String[]{"Goodbye! Have a wonderful day! 🌟", "See you later! Take care!"});
        enResponses.put("THANKYOU", new String[]{"You're welcome! I'm here to help anytime! 😊", "My pleasure! Let me know if you need anything else!"});
        enResponses.put("NAME", new String[]{"My name is VoiceAI! Nice to meet you! 🤖", "I'm called VoiceAI Assistant. What's your name?"});
        enResponses.put("ABOUT", new String[]{"I'm an AI/ML Voice Assistant built with Java and Spring Boot! 🚀", "I'm VoiceAI - your intelligent voice assistant."});
        enResponses.put("UNKNOWN", new String[]{"I'm not sure I understand. Can you rephrase?", "Sorry, I didn't catch that."});
        LANGUAGE_RESPONSES.put("en", enResponses);
        
        // German Responses
        Map<String, String[]> deResponses = new HashMap<>();
        deResponses.put("GREETING", new String[]{"Hallo! Wie kann ich Ihnen helfen? 😊", "Hi! Was kann ich für Sie tun?", "Guten Tag! Wie kann ich Ihnen helfen?"});
        deResponses.put("TIME", new String[]{"Die aktuelle Zeit ist: " + new Date(), "Es ist " + new Date() + " jetzt."});
        deResponses.put("WEATHER", new String[]{"Das Wetter ist heute schön! ☀️", "Die Vorhersage zeigt klaren Himmel."});
        deResponses.put("MUSIC", new String[]{"🎵 Ich spiele Musik für Sie!", "Lassen Sie mich die perfekte Playlist finden! 🎶"});
        deResponses.put("HELP", new String[]{"Ich kann bei Wetter, Zeit, Musik, Witzen und mehr helfen!", "Womit kann ich Ihnen helfen?"});
        deResponses.put("JOKE", new String[]{"Warum vertrauen Wissenschaftler Atomen nicht? Weil sie alles erfinden! 😂", "Wie nennt man einen Bären ohne Zähne? Einen Gummibären! 🐻"});
        deResponses.put("GOODBYE", new String[]{"Auf Wiedersehen! 🌟", "Bis später!"});
        deResponses.put("THANKYOU", new String[]{"Gern geschehen! 😊", "Mit Vergnügen!"});
        deResponses.put("NAME", new String[]{"Ich heiße VoiceAI! 🤖", "Mein Name ist VoiceAI Assistant."});
        deResponses.put("ABOUT", new String[]{"Ich bin ein AI/ML Sprachassistent! 🚀", "Ich bin VoiceAI - Ihr intelligenter Assistent."});
        deResponses.put("UNKNOWN", new String[]{"Ich verstehe nicht. Können Sie es umformulieren?", "Entschuldigung, ich habe das nicht verstanden."});
        LANGUAGE_RESPONSES.put("de", deResponses);
        
        // Hindi Responses
        Map<String, String[]> hiResponses = new HashMap<>();
        hiResponses.put("GREETING", new String[]{"नमस्ते! मैं आपकी कैसे मदद कर सकता हूँ? 😊", "नमस्कार! मैं आपके लिए क्या कर सकता हूँ?"});
        hiResponses.put("TIME", new String[]{"वर्तमान समय है: " + new Date(), "अभी " + new Date() + " बज रहे हैं।"});
        hiResponses.put("WEATHER", new String[]{"आज का मौसम बहुत सुंदर है! ☀️", "मौसम साफ और सुहावना है।"});
        hiResponses.put("MUSIC", new String[]{"🎵 आपके लिए संगीत बजा रहा हूँ!", "सही प्लेलिस्ट ढूंढता हूँ! 🎶"});
        hiResponses.put("HELP", new String[]{"मैं मौसम, समय, संगीत, चुटकुले में मदद कर सकता हूँ!", "आज मैं आपकी क्या मदद कर सकता हूँ?"});
        hiResponses.put("JOKE", new String[]{"वैज्ञानिक परमाणुओं पर भरोसा क्यों नहीं करते? क्योंकि वे सब कुछ बनाते हैं! 😂", "गमी भालू! 🐻"});
        hiResponses.put("GOODBYE", new String[]{"अलविदा! आपका दिन शुभ हो! 🌟", "फिर मिलेंगे!"});
        hiResponses.put("THANKYOU", new String[]{"आपका स्वागत है! 😊", "मुझे खुशी हुई!"});
        hiResponses.put("NAME", new String[]{"मेरा नाम VoiceAI है! 🤖", "मैं VoiceAI सहायक हूँ!"});
        hiResponses.put("ABOUT", new String[]{"मैं AI/ML वॉयस सहायक हूँ! 🚀", "मैं VoiceAI हूँ!"});
        hiResponses.put("UNKNOWN", new String[]{"मुझे समझ नहीं आया। क्या आप फिर से कह सकते हैं?", "क्षमा करें, मैं समझ नहीं सका।"});
        LANGUAGE_RESPONSES.put("hi", hiResponses);
        
        // Spanish Responses
        Map<String, String[]> esResponses = new HashMap<>();
        esResponses.put("GREETING", new String[]{"¡Hola! ¿Cómo puedo ayudarte? 😊", "¡Hola! ¿Qué puedo hacer por ti?"});
        esResponses.put("TIME", new String[]{"La hora actual es: " + new Date(), "Son las " + new Date() + " ahora mismo."});
        esResponses.put("WEATHER", new String[]{"¡El clima se ve hermoso hoy! ☀️", "El pronóstico muestra cielos despejados."});
        esResponses.put("MUSIC", new String[]{"🎵 ¡Reproduciendo música para ti!", "¡Déjame encontrar la playlist perfecta! 🎶"});
        esResponses.put("HELP", new String[]{"¡Puedo ayudar con clima, hora, música, chistes y más!", "¿En qué puedo ayudarte?"});
        esResponses.put("JOKE", new String[]{"¿Por qué los científicos no confían en los átomos? ¡Porque lo inventan todo! 😂", "¡Un osito de goma! 🐻"});
        esResponses.put("GOODBYE", new String[]{"¡Adiós! ¡Que tengas un buen día! 🌟", "¡Hasta luego!"});
        esResponses.put("THANKYOU", new String[]{"¡De nada! 😊", "¡Con gusto!"});
        esResponses.put("NAME", new String[]{"¡Mi nombre es VoiceAI! 🤖", "Soy VoiceAI Asistente."});
        esResponses.put("ABOUT", new String[]{"¡Soy un asistente de voz AI/ML! 🚀", "Soy VoiceAI."});
        esResponses.put("UNKNOWN", new String[]{"No entiendo. ¿Puedes reformular?", "Lo siento, no entendí."});
        LANGUAGE_RESPONSES.put("es", esResponses);
        
        // French Responses
        Map<String, String[]> frResponses = new HashMap<>();
        frResponses.put("GREETING", new String[]{"Bonjour! Comment puis-je vous aider? 😊", "Salut! Qu'est-ce que je peux faire?"});
        frResponses.put("TIME", new String[]{"L'heure actuelle est: " + new Date(), "Il est " + new Date() + " maintenant."});
        frResponses.put("WEATHER", new String[]{"Il fait beau aujourd'hui! ☀️", "Le temps est agréable."});
        frResponses.put("MUSIC", new String[]{"🎵 Je joue de la musique pour vous!", "Laissez-moi trouver la playlist! 🎶"});
        frResponses.put("HELP", new String[]{"Je peux aider avec la météo, l'heure, la musique, les blagues!", "Que puis-je faire pour vous?"});
        frResponses.put("JOKE", new String[]{"Pourquoi les scientifiques ne font-ils pas confiance aux atomes? Ils inventent tout! 😂", "Un ours en gélatine! 🐻"});
        frResponses.put("GOODBYE", new String[]{"Au revoir! 🌟", "À plus tard!"});
        frResponses.put("THANKYOU", new String[]{"Je vous en prie! 😊", "Avec plaisir!"});
        frResponses.put("NAME", new String[]{"Je m'appelle VoiceAI! 🤖", "Je suis VoiceAI Assistant."});
        frResponses.put("ABOUT", new String[]{"Je suis un assistant vocal AI/ML! 🚀", "Je suis VoiceAI."});
        frResponses.put("UNKNOWN", new String[]{"Je ne comprends pas. Pouvez-vous reformuler?", "Désolé, je n'ai pas compris."});
        LANGUAGE_RESPONSES.put("fr", frResponses);
        
        // Japanese Responses
        Map<String, String[]> jaResponses = new HashMap<>();
        jaResponses.put("GREETING", new String[]{"こんにちは！お手伝いできますか？😊", "こんにちは！何かお手伝いできることはありますか？"});
        jaResponses.put("TIME", new String[]{"現在の時刻は: " + new Date(), "今は " + new Date() + " です。"});
        jaResponses.put("WEATHER", new String[]{"今日は良い天気です！☀️", "天気予報では晴れです。"});
        jaResponses.put("MUSIC", new String[]{"🎵 音楽を再生しています！", "プレイリストを見つけます！🎶"});
        jaResponses.put("HELP", new String[]{"天気、時間、音楽、ジョークについてお手伝いできます！", "何かお手伝いできますか？"});
        jaResponses.put("JOKE", new String[]{"科学者はなぜ原子を信じないのか？すべてを作り上げるからです！😂", "グミベア！🐻"});
        jaResponses.put("GOODBYE", new String[]{"さようなら！🌟", "また後で！"});
        jaResponses.put("THANKYOU", new String[]{"どういたしまして！😊", "喜んで！"});
        jaResponses.put("NAME", new String[]{"私の名前はVoiceAIです！🤖", "私はVoiceAIアシスタントです。"});
        jaResponses.put("ABOUT", new String[]{"私はAI/ML音声アシスタントです！🚀", "私はVoiceAIです。"});
        jaResponses.put("UNKNOWN", new String[]{"理解できませんでした。言い換えていただけますか？", "すみません、聞き取れませんでした。"});
        LANGUAGE_RESPONSES.put("ja", jaResponses);
        
        // Chinese Responses
        Map<String, String[]> zhResponses = new HashMap<>();
        zhResponses.put("GREETING", new String[]{"你好！我能帮你什么？😊", "你好！我能为你做什么？"});
        zhResponses.put("TIME", new String[]{"当前时间是: " + new Date(), "现在是 " + new Date()});
        zhResponses.put("WEATHER", new String[]{"今天天气很好！☀️", "天气预报显示晴朗。"});
        zhResponses.put("MUSIC", new String[]{"🎵 正在播放音乐！", "让我找到播放列表！🎶"});
        zhResponses.put("HELP", new String[]{"我可以帮你处理天气、时间、音乐、笑话！", "今天我能帮你什么？"});
        zhResponses.put("JOKE", new String[]{"科学家为什么不信任原子？它们构成一切！😂", "橡皮熊！🐻"});
        zhResponses.put("GOODBYE", new String[]{"再见！🌟", "回头见！"});
        zhResponses.put("THANKYOU", new String[]{"不客气！😊", "我的荣幸！"});
        zhResponses.put("NAME", new String[]{"我叫VoiceAI！🤖", "我是VoiceAI助手。"});
        zhResponses.put("ABOUT", new String[]{"我是AI/ML语音助手！🚀", "我是VoiceAI。"});
        zhResponses.put("UNKNOWN", new String[]{"我不太明白。你能换种说法吗？", "抱歉，我没听懂。"});
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
        
        // Safely normalize language input (handles uppercase, nulls, and extra spaces)
        String langKey = (language == null || language.trim().isEmpty()) ? "en" : language.trim().toLowerCase();
        
        String msg = message.trim().toLowerCase();
        Map<String, Integer> scores = new HashMap<>();
        
        Map<String, String[]> keywords = LANGUAGE_KEYWORDS.get(langKey);
        if (keywords == null) {
            keywords = LANGUAGE_KEYWORDS.get("en");
        }
        
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
        String langKey = (language == null || language.trim().isEmpty()) ? "en" : language.trim().toLowerCase();
        Map<String, String[]> responses = LANGUAGE_RESPONSES.get(langKey);
        if (responses == null) {
            responses = LANGUAGE_RESPONSES.get("en");
        }
        
        String[] responseList = responses.getOrDefault(intent, responses.get("UNKNOWN"));
        Random random = new Random();
        return responseList[random.nextInt(responseList.length)];
    }
    
    public List<Conversation> getHistory(String sessionId) {
        return conversationRepository.findBySessionIdOrderByCreatedAtDesc(sessionId);
    }
}