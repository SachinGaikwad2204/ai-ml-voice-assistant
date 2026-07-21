package com.aiml.voice.service;

import com.aiml.voice.ml.IntentClassifier;
import com.aiml.voice.model.Conversation;
import com.aiml.voice.repository.ConversationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.util.*;

@Service
public class AutomaticTrainingService {
    
    @Autowired
    private ConversationRepository conversationRepository;
    
    @Autowired
    private IntentClassifierService intentClassifierService;
    
    private static final int MIN_SAMPLES_FOR_TRAINING = 5;
    private static final long AUTO_TRAIN_INTERVAL = 60000;
    private static final int EPOCHS = 1000;  // Increased from 200
    private static final double LEARNING_RATE = 0.01;  // Increased from 0.1
    
    @Scheduled(fixedDelay = AUTO_TRAIN_INTERVAL)
    public void autoTrain() {
        System.out.println("Running automatic training check...");
        try {
            List<Conversation> untrained = conversationRepository.findByUsedForTrainingFalse();
            System.out.println("Found " + untrained.size() + " untrained conversations");
            
            if (untrained.size() < MIN_SAMPLES_FOR_TRAINING) {
                System.out.println("Not enough samples. Need " + MIN_SAMPLES_FOR_TRAINING + ", have " + untrained.size());
                return;
            }
            
            System.out.println("Found " + untrained.size() + " new samples - starting training...");
            trainFromUserData(untrained);
            markAsTrained(untrained);
            System.out.println("Auto-training completed successfully!");
        } catch (Exception e) {
            System.err.println("Auto-training failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Transactional
    public void trainFromUserData(List<Conversation> conversations) throws Exception {
        List<String> texts = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        
        System.out.println("Processing " + conversations.size() + " conversations for training...");
        
        // Add the new untrained conversations
        for (Conversation conv : conversations) {
            String message = conv.getUserMessage();
            String intent = conv.getIntent();
            if (message != null && !message.trim().isEmpty() && 
                intent != null && !intent.trim().isEmpty()) {
                texts.add(message);
                labels.add(intent);
            }
        }
        
        // Also get all previously trained conversations
        List<Conversation> allConversations = conversationRepository.findAll();
        for (Conversation conv : allConversations) {
            if (conv.isUsedForTraining()) {
                String message = conv.getUserMessage();
                String intent = conv.getIntent();
                if (message != null && !message.trim().isEmpty() && 
                    intent != null && !intent.trim().isEmpty()) {
                    texts.add(message);
                    labels.add(intent);
                }
            }
        }
        
        if (texts.isEmpty()) {
            System.out.println("No valid training samples found");
            return;
        }
        
        System.out.println("Training with " + texts.size() + " total samples");
        System.out.println("Epochs: " + EPOCHS + ", Learning Rate: " + LEARNING_RATE);
        
        // Load or create classifier
        IntentClassifier classifier;
        File modelFile = new File("models/classifier.ser");
        if (modelFile.exists()) {
            try {
                classifier = IntentClassifier.loadModel("models/classifier.ser");
                System.out.println("Loaded existing model");
            } catch (Exception e) {
                System.err.println("Failed to load existing model, creating new one: " + e.getMessage());
                classifier = new IntentClassifier();
            }
        } else {
            classifier = new IntentClassifier();
            System.out.println("Creating new model");
        }
        
        String[] textArray = texts.toArray(new String[0]);
        String[] labelArray = labels.toArray(new String[0]);
        System.out.println("Training with " + textArray.length + " samples");
        
        classifier.train(textArray, labelArray, EPOCHS, LEARNING_RATE);
        
        File modelsDir = new File("models");
        if (!modelsDir.exists()) {
            modelsDir.mkdirs();
        }
        classifier.saveModel("models/classifier.ser");
        System.out.println("Model saved to models/classifier.ser");
        
        if (intentClassifierService != null) {
            intentClassifierService.reloadModel();
            System.out.println("Model reloaded in service");
        }
        
        System.out.println("Training completed successfully!");
    }
    
    @Transactional
    public void markAsTrained(List<Conversation> conversations) {
        if (conversations == null || conversations.isEmpty()) {
            return;
        }
        for (Conversation conv : conversations) {
            conv.setUsedForTraining(true);
        }
        conversationRepository.saveAll(conversations);
        System.out.println("Marked " + conversations.size() + " conversations as trained");
    }
    
    public String trainNow() {
        try {
            List<Conversation> untrained = conversationRepository.findByUsedForTrainingFalse();
            System.out.println("trainNow: Found " + untrained.size() + " untrained conversations");
            
            if (untrained.isEmpty()) {
                long total = conversationRepository.count();
                if (total == 0) {
                    return "No conversations in database. Please send some messages first.";
                } else {
                    return "All " + total + " conversations are already trained. Send new messages to train.";
                }
            }
            
            if (untrained.size() < MIN_SAMPLES_FOR_TRAINING) {
                return "Need at least " + MIN_SAMPLES_FOR_TRAINING + " untrained samples. Have " + untrained.size() + ". Send more messages.";
            }
            
            trainFromUserData(untrained);
            markAsTrained(untrained);
            return "Training completed with " + untrained.size() + " samples (Epochs: " + EPOCHS + ", LR: " + LEARNING_RATE + ")";
        } catch (Exception e) {
            e.printStackTrace();
            return "Training failed: " + e.getMessage();
        }
    }
    
    public Map<String, Object> getTrainingStatus() {
        Map<String, Object> status = new HashMap<>();
        try {
            long total = conversationRepository.count();
            long untrained = conversationRepository.countByUsedForTrainingFalse();
            status.put("totalConversations", total);
            status.put("trained", total - untrained);
            status.put("untrained", untrained);
            status.put("minSamplesRequired", MIN_SAMPLES_FOR_TRAINING);
            status.put("modelExists", new File("models/classifier.ser").exists());
            status.put("modelPath", "models/classifier.ser");
            status.put("epochs", EPOCHS);
            status.put("learningRate", LEARNING_RATE);
        } catch (Exception e) {
            status.put("error", e.getMessage());
        }
        return status;
    }
}