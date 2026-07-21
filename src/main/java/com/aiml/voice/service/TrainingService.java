package com.aiml.voice.service;

import com.aiml.voice.ml.IntentClassifier;
import org.springframework.stereotype.Service;
import java.io.*;

@Service
public class TrainingService {
    public String trainModel() {
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
            classifier.train(texts, labels, 500, 0.1);
            new File("models").mkdirs();
            classifier.saveModel("models/classifier.ser");
            return "Model trained successfully!";
        } catch (Exception e) {
            return "Training failed: " + e.getMessage();
        }
    }
}