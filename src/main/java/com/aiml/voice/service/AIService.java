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
        
        // ===== LANGUAGE RESPONSES =====
        // English Responses
        Map<String, String[]> enResponses = new HashMap<>();
        enResponses.put("GREETING", new String[]{"Hello! How can I help you today? 😊", "Hi there! What can I do for you?", "Hey! Great to see you! How can I assist you?"});
        enResponses.put("TIME", new String[]{"The current time is: " + new Date(), "It's " + new Date() + " right now.", "The time is " + new Date()});
        enResponses.put("WEATHER", new String[]{"I'm checking the weather. It looks beautiful today! ☀️", "The forecast shows clear skies and pleasant weather.", "Weather looks great! Perfect day ahead."});
        enResponses.put("MUSIC", new String[]{"🎵 Playing some great music for you!", "Let me find the perfect playlist for you! 🎶", "I'd love to play some music! Enjoy! 🎵"});
        enResponses.put("HELP", new String[]{"I'm here to help! I can assist with weather, time, music, jokes, and more!", "What can I help you with today? Just ask me anything!", "I'm your personal assistant! I can handle various tasks for you."});
        enResponses.put("JOKE", new String[]{"Why don't scientists trust atoms? Because they make up everything! 😂", "What do you call a bear with no teeth? A gummy bear! 🐻", "Why did the chicken cross the road? To get to the other side! 🐔"});
        enResponses.put("GOODBYE", new String[]{"Goodbye! Have a wonderful day! 🌟", "See you later! Take care!", "Bye for now! Come back anytime!"});
        enResponses.put("THANKYOU", new String[]{"You're welcome! I'm here to help anytime! 😊", "My pleasure! Let me know if you need anything else!", "Anytime! Happy to assist!"});
        enResponses.put("NAME", new String[]{"My name is VoiceAI! Nice to meet you! 🤖", "I'm called VoiceAI Assistant. What's your name?", "I'm your friendly AI Voice Assistant!"});
        enResponses.put("ABOUT", new String[]{"I'm an AI/ML Voice Assistant built with Java and Spring Boot! 🚀", "I'm VoiceAI - your intelligent voice assistant powered by machine learning.", "I can help with weather, time, music, jokes, and more! Just ask!"});
        enResponses.put("UNKNOWN", new String[]{"I'm not sure I understand. Can you rephrase?", "Sorry, I didn't catch that. Could you say it differently?", "I'm still learning! Could you try asking in a different way?"});
        LANGUAGE_RESPONSES.put("en", enResponses);
        
        // Hindi Responses
        Map<String, String[]> hiResponses = new HashMap<>();
        hiResponses.put("GREETING", new String[]{"नमस्ते! मैं आपकी कैसे मदद कर सकता हूँ? 😊", "नमस्कार! मैं आपके लिए क्या कर सकता हूँ?", "हे! आपको देखकर अच्छा लगा! मैं आपकी कैसे सहायता कर सकता हूँ?"});
        hiResponses.put("TIME", new String[]{"वर्तमान समय है: " + new Date(), "अभी " + new Date() + " बज रहे हैं।", "समय " + new Date() + " है"});
        hiResponses.put("WEATHER", new String[]{"मैं मौसम की जाँच कर रहा हूँ। आज का मौसम बहुत सुंदर है! ☀️", "मौसम पूर्वानुमान साफ आसमान और सुहावना मौसम दिखा रहा है।", "मौसम बहुत अच्छा है! आज का दिन शानदार है।"});
        hiResponses.put("MUSIC", new String[]{"🎵 आपके लिए बढ़िया संगीत बजा रहा हूँ!", "आपके लिए सही प्लेलिस्ट ढूंढता हूँ! 🎶", "मुझे संगीत बजाना अच्छा लगता है! आनंद लें! 🎵"});
        hiResponses.put("HELP", new String[]{"मैं मदद के लिए यहाँ हूँ! मैं मौसम, समय, संगीत, चुटकुले और बहुत कुछ में सहायता कर सकता हूँ!", "आज मैं आपकी क्या मदद कर सकता हूँ? बस मुझसे कुछ भी पूछें!", "मैं आपका व्यक्तिगत सहायक हूँ! मैं आपके लिए विभिन्न कार्य कर सकता हूँ।"});
        hiResponses.put("JOKE", new String[]{"वैज्ञानिक परमाणुओं पर भरोसा क्यों नहीं करते? क्योंकि वे सब कुछ बनाते हैं! 😂", "उस भालू को क्या कहते हैं जिसके दाँत नहीं हैं? गमी भालू! 🐻", "मुर्गी ने सड़क क्यों पार की? दूसरी तरफ जाने के लिए! 🐔"});
        hiResponses.put("GOODBYE", new String[]{"अलविदा! आपका दिन शुभ हो! 🌟", "फिर मिलेंगे! अपना ख्याल रखें!", "अभी के लिए अलविदा! कभी भी वापस आएं!"});
        hiResponses.put("THANKYOU", new String[]{"आपका स्वागत है! मैं कभी भी मदद के लिए यहाँ हूँ! 😊", "मुझे खुशी हुई! अगर कुछ और चाहिए तो बताएं!", "कभी भी! खुशी से मदद करूँगा!"});
        hiResponses.put("NAME", new String[]{"मेरा नाम VoiceAI है! आपसे मिलकर अच्छा लगा! 🤖", "मुझे VoiceAI सहायक कहा जाता है। आपका नाम क्या है?", "मैं आपका मित्रवत AI वॉयस सहायक हूँ!"});
        hiResponses.put("ABOUT", new String[]{"मैं Java और Spring Boot के साथ बनाया गया AI/ML वॉयस सहायक हूँ! 🚀", "मैं VoiceAI हूँ - मशीन लर्निंग द्वारा संचालित आपका बुद्धिमान वॉयस सहायक।", "मैं मौसम, समय, संगीत, चुटकुले और बहुत कुछ में मदद कर सकता हूँ! बस पूछें!"});
        hiResponses.put("UNKNOWN", new String[]{"मुझे समझ नहीं आया। क्या आप फिर से कह सकते हैं?", "क्षमा करें, मैं वह नहीं समझ सका। क्या आप इसे अलग तरीके से कह सकते हैं?", "मैं अभी भी सीख रहा हूँ! क्या आप इसे अलग तरीके से पूछ सकते हैं?"});
        LANGUAGE_RESPONSES.put("hi", hiResponses);
        
        // Spanish Responses
        Map<String, String[]> esResponses = new HashMap<>();
        esResponses.put("GREETING", new String[]{"¡Hola! ¿Cómo puedo ayudarte hoy? 😊", "¡Hola! ¿Qué puedo hacer por ti?", "¡Hey! ¡Qué bueno verte! ¿Cómo puedo ayudarte?"});
        esResponses.put("TIME", new String[]{"La hora actual es: " + new Date(), "Son las " + new Date() + " ahora mismo.", "La hora es " + new Date()});
        esResponses.put("WEATHER", new String[]{"Estoy revisando el clima. ¡Se ve hermoso hoy! ☀️", "El pronóstico muestra cielos despejados y clima agradable.", "¡El clima se ve genial! Día perfecto."});
        esResponses.put("MUSIC", new String[]{"🎵 ¡Reproduciendo buena música para ti!", "¡Déjame encontrar la lista de reproducción perfecta para ti! 🎶", "¡Me encantaría reproducir música! ¡Disfruta! 🎵"});
        esResponses.put("HELP", new String[]{"¡Estoy aquí para ayudar! ¡Puedo ayudar con el clima, la hora, la música, chistes y más!", "¿En qué puedo ayudarte hoy? ¡Solo pregúntame cualquier cosa!", "¡Soy tu asistente personal! Puedo manejar varias tareas para ti."});
        esResponses.put("JOKE", new String[]{"¿Por qué los científicos no confían en los átomos? ¡Porque lo inventan todo! 😂", "¿Cómo se llama un oso sin dientes? ¡Un osito de goma! 🐻", "¿Por qué cruzó el pollo la carretera? ¡Para llegar al otro lado! 🐔"});
        esResponses.put("GOODBYE", new String[]{"¡Adiós! ¡Que tengas un día maravilloso! 🌟", "¡Hasta luego! ¡Cuídate!", "¡Adiós por ahora! ¡Vuelve cuando quieras!"});
        esResponses.put("THANKYOU", new String[]{"¡De nada! ¡Estoy aquí para ayudar en cualquier momento! 😊", "¡Con gusto! ¡Déjame saber si necesitas algo más!", "¡Cuando quieras! ¡Feliz de ayudar!"});
        esResponses.put("NAME", new String[]{"¡Mi nombre es VoiceAI! ¡Mucho gusto! 🤖", "¡Me llamo VoiceAI Asistente! ¿Cómo te llamas?", "¡Soy tu amigable Asistente de Voz AI!"});
        esResponses.put("ABOUT", new String[]{"¡Soy un asistente de voz AI/ML construido con Java y Spring Boot! 🚀", "¡Soy VoiceAI, tu inteligente asistente de voz impulsado por machine learning!", "¡Puedo ayudar con clima, hora, música, chistes y más! ¡Solo pregunta!"});
        esResponses.put("UNKNOWN", new String[]{"No estoy seguro de entender. ¿Puedes reformular?", "Lo siento, no entendí eso. ¿Podrías decirlo de otra manera?", "¡Todavía estoy aprendiendo! ¿Podrías intentar preguntar de otra manera?"});
        LANGUAGE_RESPONSES.put("es", esResponses);
        
        // French Responses
        Map<String, String[]> frResponses = new HashMap<>();
        frResponses.put("GREETING", new String[]{"Bonjour! Comment puis-je vous aider aujourd'hui? 😊", "Salut! Qu'est-ce que je peux faire pour vous?", "Bonjour! Ravie de vous voir! Comment puis-je vous assister?"});
        frResponses.put("TIME", new String[]{"L'heure actuelle est: " + new Date(), "Il est " + new Date() + " maintenant.", "L'heure est " + new Date()});
        frResponses.put("WEATHER", new String[]{"Je vérifie la météo. Il fait beau aujourd'hui! ☀️", "Les prévisions montrent un ciel dégagé et un temps agréable.", "Le temps est super! Journée parfaite."});
        frResponses.put("MUSIC", new String[]{"🎵 Je joue de la bonne musique pour vous!", "Laissez-moi trouver la playlist parfaite pour vous! 🎶", "J'aimerais jouer de la musique! Profitez-en! 🎵"});
        frResponses.put("HELP", new String[]{"Je suis là pour vous aider! Je peux vous aider avec la météo, l'heure, la musique, les blagues et plus encore!", "Que puis-je faire pour vous aujourd'hui? Demandez-moi n'importe quoi!", "Je suis votre assistant personnel! Je peux gérer diverses tâches pour vous."});
        frResponses.put("JOKE", new String[]{"Pourquoi les scientifiques ne font-ils pas confiance aux atomes? Parce qu'ils inventent tout! 😂", "Comment appelle-t-on un ours sans dents? Un ours en gélatine! 🐻", "Pourquoi le poulet a-t-il traversé la route? Pour aller de l'autre côté! 🐔"});
        frResponses.put("GOODBYE", new String[]{"Au revoir! Passez une merveilleuse journée! 🌟", "À plus tard! Prenez soin de vous!", "Salut pour l'instant! Revenez quand vous voulez!"});
        frResponses.put("THANKYOU", new String[]{"Je vous en prie! Je suis là pour vous aider à tout moment! 😊", "Avec plaisir! Faites-moi savoir si vous avez besoin d'autre chose!", "Quand vous voulez! Heureux de vous aider!"});
        frResponses.put("NAME", new String[]{"Je m'appelle VoiceAI! Enchanté de vous rencontrer! 🤖", "Je suis VoiceAI Assistant. Comment vous appelez-vous?", "Je suis votre assistant vocal AI convivial!"});
        frResponses.put("ABOUT", new String[]{"Je suis un assistant vocal AI/ML construit avec Java et Spring Boot! 🚀", "Je suis VoiceAI - votre assistant vocal intelligent alimenté par le machine learning!", "Je peux aider avec la météo, l'heure, la musique, les blagues et plus encore! Demandez-moi n'importe quoi!"});
        frResponses.put("UNKNOWN", new String[]{"Je ne suis pas sûr de comprendre. Pouvez-vous reformuler?", "Désolé, je n'ai pas compris. Pourriez-vous le dire différemment?", "J'apprends encore! Pourriez-vous essayer de demander d'une manière différente?"});
        LANGUAGE_RESPONSES.put("fr", frResponses);
        
        // German Responses
        Map<String, String[]> deResponses = new HashMap<>();
        deResponses.put("GREETING", new String[]{"Hallo! Wie kann ich Ihnen heute helfen? 😊", "Hallo! Was kann ich für Sie tun?", "Hey! Schön Sie zu sehen! Wie kann ich Ihnen helfen?"});
        deResponses.put("TIME", new String[]{"Die aktuelle Zeit ist: " + new Date(), "Es ist " + new Date() + " jetzt.", "Die Zeit ist " + new Date()});
        deResponses.put("WEATHER", new String[]{"Ich überprüfe das Wetter. Es sieht heute schön aus! ☀️", "Die Vorhersage zeigt klaren Himmel und angenehmes Wetter.", "Das Wetter sieht großartig aus! Perfekter Tag."});
        deResponses.put("MUSIC", new String[]{"🎵 Ich spiele großartige Musik für Sie!", "Lassen Sie mich die perfekte Playlist für Sie finden! 🎶", "Ich würde gerne Musik spielen! Genießen Sie es! 🎵"});
        deResponses.put("HELP", new String[]{"Ich bin hier, um zu helfen! Ich kann bei Wetter, Zeit, Musik, Witzen und mehr helfen!", "Womit kann ich Ihnen heute helfen? Fragen Sie mich einfach alles!", "Ich bin Ihr persönlicher Assistent! Ich kann verschiedene Aufgaben für Sie übernehmen."});
        deResponses.put("JOKE", new String[]{"Warum vertrauen Wissenschaftler Atomen nicht? Weil sie alles erfinden! 😂", "Wie nennt man einen Bären ohne Zähne? Einen Gummibären! 🐻", "Warum hat das Huhn die Straße überquert? Um auf die andere Seite zu gelangen! 🐔"});
        deResponses.put("GOODBYE", new String[]{"Auf Wiedersehen! Haben Sie einen wundervollen Tag! 🌟", "Bis später! Passen Sie auf sich auf!", "Tschüss für jetzt! Kommen Sie jederzeit wieder!"});
        deResponses.put("THANKYOU", new String[]{"Gern geschehen! Ich bin jederzeit hier, um zu helfen! 😊", "Mit Vergnügen! Lassen Sie mich wissen, wenn Sie etwas anderes brauchen!", "Jederzeit! Gerne helfe ich!"});
        deResponses.put("NAME", new String[]{"Mein Name ist VoiceAI! Schön, Sie kennenzulernen! 🤖", "Ich heiße VoiceAI Assistant. Wie heißen Sie?", "Ich bin Ihr freundlicher AI-Sprachassistent!"});
        deResponses.put("ABOUT", new String[]{"Ich bin ein AI/ML-Sprachassistent, der mit Java und Spring Boot erstellt wurde! 🚀", "Ich bin VoiceAI - Ihr intelligenter Sprachassistent mit Machine Learning!", "Ich kann bei Wetter, Zeit, Musik, Witzen und mehr helfen! Fragen Sie mich einfach alles!"});
        deResponses.put("UNKNOWN", new String[]{"Ich bin mir nicht sicher, ob ich verstehe. Können Sie es umformulieren?", "Entschuldigung, ich habe das nicht verstanden. Könnten Sie es anders sagen?", "Ich lerne noch! Könnten Sie es auf eine andere Weise versuchen?"});
        LANGUAGE_RESPONSES.put("de", deResponses);
        
        // Japanese Responses
        Map<String, String[]> jaResponses = new HashMap<>();
        jaResponses.put("GREETING", new String[]{"こんにちは！今日はどのようにお手伝いできますか？😊", "こんにちは！何かお手伝いできることはありますか？", "やあ！お会いできて嬉しいです！どのようにお手伝いできますか？"});
        jaResponses.put("TIME", new String[]{"現在の時刻は: " + new Date(), "今は " + new Date() + " です。", "時間は " + new Date() + " です"});
        jaResponses.put("WEATHER", new String[]{"天気を確認しています。今日はとても良い天気です！☀️", "予報では晴れで快適な天気です。", "天気は素晴らしいです！完璧な一日です。"});
        jaResponses.put("MUSIC", new String[]{"🎵 素晴らしい音楽を再生しています！", "あなたにぴったりのプレイリストを見つけます！🎶", "音楽を再生するのが楽しみです！お楽しみください！🎵"});
        jaResponses.put("HELP", new String[]{"私はお手伝いするためにここにいます！天気、時間、音楽、ジョークなどについてお手伝いできます！", "今日は何かお手伝いできますか？何でも聞いてください！", "私はあなたのパーソナルアシスタントです！さまざまなタスクを処理できます。"});
        jaResponses.put("JOKE", new String[]{"科学者はなぜ原子を信じないのか？すべてを作り上げるからです！😂", "歯のない熊を何と呼ぶ？グミベア！🐻", "鶏はなぜ道路を横断したのか？反対側に行くためです！🐔"});
        jaResponses.put("GOODBYE", new String[]{"さようなら！素晴らしい一日を！🌟", "また後で！お元気で！", "また今度！いつでも戻ってきてください！"});
        jaResponses.put("THANKYOU", new String[]{"どういたしまして！いつでもお手伝いします！😊", "喜んで！何か他に必要なものがあったら教えてください！", "いつでも！喜んでお手伝いします！"});
        jaResponses.put("NAME", new String[]{"私の名前はVoiceAIです！お会いできて嬉しいです！🤖", "私はVoiceAIアシスタントです。お名前は何ですか？", "私はあなたのフレンドリーなAI音声アシスタントです！"});
        jaResponses.put("ABOUT", new String[]{"私はJavaとSpring Bootで構築されたAI/ML音声アシスタントです！🚀", "私はVoiceAIです - 機械学習を搭載したインテリジェントな音声アシスタント！", "天気、時間、音楽、ジョークなどについてお手伝いできます！何でも聞いてください！"});
        jaResponses.put("UNKNOWN", new String[]{"理解できませんでした。言い換えていただけますか？", "すみません、聞き取れませんでした。別の言い方をしていただけますか？", "まだ学習中です！別の方法で質問してみてください。"});
        LANGUAGE_RESPONSES.put("ja", jaResponses);
        
        // Chinese Responses
        Map<String, String[]> zhResponses = new HashMap<>();
        zhResponses.put("GREETING", new String[]{"你好！今天我能帮你什么？😊", "你好！我能为你做什么？", "嘿！很高兴见到你！我能怎么帮你？"});
        zhResponses.put("TIME", new String[]{"当前时间是: " + new Date(), "现在是 " + new Date(), "时间是 " + new Date()});
        zhResponses.put("WEATHER", new String[]{"我正在检查天气。今天天气很好！☀️", "预报显示晴朗和宜人的天气。", "天气看起来很棒！完美的一天。"});
        zhResponses.put("MUSIC", new String[]{"🎵 正在为你播放好音乐！", "让我为你找到完美的播放列表！🎶", "我很想放音乐！享受吧！🎵"});
        zhResponses.put("HELP", new String[]{"我在这里帮忙！我可以帮你处理天气、时间、音乐、笑话等！", "今天我能帮你什么？尽管问我！", "我是你的个人助手！我可以为你处理各种任务。"});
        zhResponses.put("JOKE", new String[]{"科学家为什么不信任原子？因为它们构成一切！😂", "没有牙齿的熊叫什么？橡皮熊！🐻", "鸡为什么过马路？为了到另一边！🐔"});
        zhResponses.put("GOODBYE", new String[]{"再见！祝你有美好的一天！🌟", "回头见！保重！", "拜拜！随时回来！"});
        zhResponses.put("THANKYOU", new String[]{"不客气！我随时都在这里帮你！😊", "我的荣幸！如果你还需要什么，请告诉我！", "随时！很高兴帮忙！"});
        zhResponses.put("NAME", new String[]{"我叫VoiceAI！很高兴认识你！🤖", "我是VoiceAI助手。你叫什么名字？", "我是你友好的AI语音助手！"});
        zhResponses.put("ABOUT", new String[]{"我是用Java和Spring Boot构建的AI/ML语音助手！🚀", "我是VoiceAI - 由机器学习驱动的智能语音助手！", "我可以帮你看天气、时间、音乐、笑话等！尽管问！"});
        zhResponses.put("UNKNOWN", new String[]{"我不太明白。你能换种说法吗？", "抱歉，我没听懂。你能换个说法吗？", "我还在学习！你能换种方式问吗？"});
        LANGUAGE_RESPONSES.put("zh", zhResponses);
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
        
        String msg = message.trim();
        Map<String, Integer> scores = new HashMap<>();
        
        // Get keywords for the language
        Map<String, String[]> keywords = LANGUAGE_KEYWORDS.get(language);
        if (keywords == null) {
            // Fallback to English if language not supported
            keywords = LANGUAGE_KEYWORDS.get("en");
        }
        
        for (Map.Entry<String, String[]> entry : keywords.entrySet()) {
            String intent = entry.getKey();
            String[] words = entry.getValue();
            int score = 0;
            
            for (String word : words) {
                if (language.equals("en")) {
                    // English: case-insensitive check
                    if (msg.toLowerCase().contains(word.toLowerCase())) {
                        score += 2;
                    }
                } else {
                    // Other languages: exact match (case-sensitive)
                    if (msg.contains(word)) {
                        score += 2;
                    }
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
        // Get language-specific responses
        Map<String, String[]> responses = LANGUAGE_RESPONSES.get(language);
        if (responses == null) {
            // Fallback to English
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
