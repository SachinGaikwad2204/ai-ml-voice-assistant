package com.aiml.voice.ml;

import java.io.*;

public class ModelTrainer {
    public static void main(String[] args) {
        System.out.println("🧠 Training AI/ML Voice Assistant Model");
        System.out.println("==========================================");
        try {
            IntentClassifier classifier = new IntentClassifier();
            String[] texts = {
                "hello", "hi", "hey", "good morning", "what time is it",
                "tell me the time", "weather", "play music", "help me",
                "goodbye", "bye", "who are you", "what is AI",
                "your name", "tell me a joke", "thank you", "search",
                "calculate 5 + 3", "translate"
            };
            String[] labels = {
                "GREETING", "GREETING", "GREETING", "GREETING", "TIME",
                "TIME", "WEATHER", "MUSIC", "HELP",
                "GOODBYE", "GOODBYE", "ABOUT", "AI",
                "NAME", "JOKE", "THANKYOU", "SEARCH",
                "CALCULATE", "TRANSLATE"
            };
            System.out.println("📊 Training with " + texts.length + " samples");
            classifier.train(texts, labels, 500, 0.1);
            new File("models").mkdirs();
            classifier.saveModel("models/classifier.ser");
            System.out.println("✅ Training complete!");
            System.out.println("📁 Model saved to: models/classifier.ser");
        } catch (Exception e) {
            System.err.println("❌ Training failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}