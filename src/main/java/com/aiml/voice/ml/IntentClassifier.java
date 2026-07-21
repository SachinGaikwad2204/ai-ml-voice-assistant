package com.aiml.voice.ml;

import com.aiml.voice.nlp.TextProcessor;
import java.io.*;
import java.util.*;

public class IntentClassifier implements Serializable {
    private static final long serialVersionUID = 2L;
    
    private NeuralNetwork model;
    private transient TextProcessor textProcessor;
    private String[] intents;
    private Map<String, Integer> intentIndex;
    private int inputSize = 200;
    private int hiddenSize = 100;
    private boolean isInitialized = false;
    
    public IntentClassifier() {
        initialize();
    }
    
    private void initialize() {
        if (textProcessor == null) {
            textProcessor = new TextProcessor(200);
        }
        if (intents == null) {
            intents = new String[]{
                "GREETING", "TIME", "WEATHER", "MUSIC", "HELP",
                "GOODBYE", "ABOUT", "AI", "NAME", "JOKE",
                "THANKYOU", "SEARCH", "CALCULATE", "TRANSLATE", "UNKNOWN"
            };
        }
        if (intentIndex == null) {
            intentIndex = new HashMap<>();
            for (int i = 0; i < intents.length; i++) {
                intentIndex.put(intents[i], i);
            }
        }
        if (model == null) {
            model = new NeuralNetwork(inputSize, hiddenSize, intents.length);
        }
        isInitialized = true;
    }

    public void train(String[] texts, String[] labels, int epochs, double learningRate) {
        initialize();
        System.out.println("\n" + "=".repeat(50));
        System.out.println("🧠 Training Intent Classifier");
        System.out.println("=".repeat(50));
        System.out.println("📊 Samples: " + texts.length);
        System.out.println("🎯 Intents: " + intents.length);
        
        // Build vocabulary from all training data
        textProcessor.buildVocabulary(texts);
        System.out.println("📚 Vocabulary size: " + textProcessor.getVocabulary().size());
        
        double[][] features = new double[texts.length][inputSize];
        double[][] targets = new double[texts.length][intents.length];
        
        for (int i = 0; i < texts.length; i++) {
            features[i] = textProcessor.textToVector(texts[i], inputSize);
            int idx = intentIndex.getOrDefault(labels[i], intents.length - 1);
            targets[i][idx] = 1.0;
        }
        
        model.train(features, targets, epochs, learningRate);
        System.out.println("=".repeat(50));
        System.out.println("✅ Training completed successfully!");
        System.out.println("=".repeat(50) + "\n");
    }

    public String classify(String text) {
        initialize();
        double[] vector = textProcessor.textToVector(text, inputSize);
        double[] output = model.forward(vector);
        
        int predictedIdx = 0;
        double maxProb = output[0];
        for (int i = 1; i < output.length; i++) {
            if (output[i] > maxProb) {
                maxProb = output[i];
                predictedIdx = i;
            }
        }
        return intents[predictedIdx];
    }

    public String getResponse(String intent) {
        initialize();
        Random random = new Random();
        
        Map<String, String[]> responseMap = new HashMap<>();
        responseMap.put("GREETING", new String[]{
            "Hello! How can I help you today? 😊",
            "Hi there! What can I do for you?",
            "Hey! Great to see you! How can I assist?",
            "Greetings! How may I help you today?"
        });
        responseMap.put("TIME", new String[]{
            "The current time is: " + new Date(),
            "It's " + new Date() + " right now.",
            "The time is " + new Date()
        });
        responseMap.put("WEATHER", new String[]{
            "I'm checking the weather. It looks beautiful today! ☀️",
            "The forecast shows clear skies and pleasant weather.",
            "Weather looks great! Perfect day ahead.",
            "Let me check... The weather is wonderful today!"
        });
        responseMap.put("MUSIC", new String[]{
            "🎵 Playing some great music for you!",
            "Let me find the perfect playlist for you! 🎶",
            "I'd love to play some music! Enjoy! 🎵",
            "🎧 Music coming right up! What would you like to hear?"
        });
        responseMap.put("HELP", new String[]{
            "I'm here to help! I can assist with weather, time, music, jokes, and more!",
            "What can I help you with today? Just ask me anything!",
            "I'm your personal assistant! I can handle various tasks for you."
        });
        responseMap.put("GOODBYE", new String[]{
            "Goodbye! Have a wonderful day! 🌟",
            "See you later! Take care!",
            "Bye for now! Come back anytime!",
            "Until next time! Have a great day!"
        });
        responseMap.put("ABOUT", new String[]{
            "I'm an AI/ML Voice Assistant built with Java and Spring Boot!",
            "I'm VoiceAI - your intelligent voice assistant powered by machine learning.",
            "I'm a virtual assistant that learns from conversations!"
        });
        responseMap.put("AI", new String[]{
            "I'm powered by Artificial Intelligence and Neural Networks!",
            "AI helps me understand and respond to your messages.",
            "I use machine learning to improve my responses over time."
        });
        responseMap.put("NAME", new String[]{
            "My name is VoiceAI! Nice to meet you! 🤖",
            "I'm called VoiceAI Assistant. What's your name?",
            "I'm your friendly AI Voice Assistant!"
        });
        responseMap.put("JOKE", new String[]{
            "Why don't scientists trust atoms? Because they make up everything! 😂",
            "What do you call a bear with no teeth? A gummy bear! 🐻",
            "Why did the chicken cross the road? To get to the other side! 🐔",
            "I told my computer I needed a break. Now it won't stop giving me space! 💻"
        });
        responseMap.put("THANKYOU", new String[]{
            "You're welcome! I'm here to help anytime! 😊",
            "My pleasure! Let me know if you need anything else!",
            "Anytime! Happy to assist!"
        });
        responseMap.put("SEARCH", new String[]{
            "Let me search that for you! 🔍",
            "I'll look that up right now!",
            "Searching for the information you need..."
        });
        responseMap.put("CALCULATE", new String[]{
            "Let me calculate that for you. 📊",
            "Computing the result...",
            "The calculation is complete."
        });
        responseMap.put("TRANSLATE", new String[]{
            "I'll translate that for you. 🌐",
            "Translating now...",
            "Here's the translation."
        });
        responseMap.put("UNKNOWN", new String[]{
            "I'm not sure I understand. Can you rephrase?",
            "Sorry, I didn't catch that. Could you say it differently?",
            "I'm still learning! Could you try asking in a different way?"
        });
        
        String[] responses = responseMap.getOrDefault(intent, responseMap.get("UNKNOWN"));
        return responses[random.nextInt(responses.length)];
    }

    public void saveModel(String path) throws IOException {
        initialize();
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path))) {
            oos.writeObject(this);
        }
        System.out.println("💾 Model saved to: " + path);
    }

    public static IntentClassifier loadModel(String path) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path))) {
            IntentClassifier classifier = (IntentClassifier) ois.readObject();
            classifier.initialize();
            return classifier;
        }
    }
}