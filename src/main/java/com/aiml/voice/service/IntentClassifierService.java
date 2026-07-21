package com.aiml.voice.service;

import com.aiml.voice.ml.IntentClassifier;
import org.springframework.stereotype.Service;
import java.io.*;

@Service
public class IntentClassifierService {
    private IntentClassifier classifier;
    
    public IntentClassifierService() {
        loadModel();
    }
    
    private void loadModel() {
        try {
            File modelFile = new File("models/classifier.ser");
            if (modelFile.exists()) {
                classifier = IntentClassifier.loadModel("models/classifier.ser");
                System.out.println("Model loaded successfully!");
            } else {
                System.out.println("No model found. Creating new model...");
                classifier = new IntentClassifier();
                System.out.println("Send messages and training will happen automatically.");
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading model: " + e.getMessage());
            classifier = new IntentClassifier();
            System.out.println("Created new classifier as fallback.");
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            classifier = new IntentClassifier();
        }
    }
    
    public String classify(String text) {
        if (classifier == null) {
            classifier = new IntentClassifier();
        }
        return classifier.classify(text);
    }
    
    public String getResponse(String intent) {
        if (classifier == null) {
            classifier = new IntentClassifier();
        }
        return classifier.getResponse(intent);
    }
    
    public void reloadModel() {
        loadModel();
    }
    
    public IntentClassifier getClassifier() {
        if (classifier == null) {
            classifier = new IntentClassifier();
        }
        return classifier;
    }
}