package com.aiml.voice.nlp;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class TextProcessor implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Map<String, Integer> vocabulary;
    private int maxFeatures = 200; // Increased for better representation
    private Map<String, Double> idfWeights;
    
    public TextProcessor() {
        this.vocabulary = new HashMap<>();
        this.idfWeights = new HashMap<>();
    }
    
    public TextProcessor(int maxFeatures) {
        this.vocabulary = new HashMap<>();
        this.idfWeights = new HashMap<>();
        this.maxFeatures = maxFeatures;
    }
    
    public void buildVocabulary(String[] texts) {
        Map<String, Integer> wordCount = new HashMap<>();
        Map<String, Integer> docCount = new HashMap<>();
        int totalDocs = texts.length;
        
        for (String text : texts) {
            Set<String> uniqueWords = new HashSet<>();
            String[] words = text.toLowerCase().replaceAll("[^a-zA-Z\\s]", "").split("\\s+");
            
            for (String word : words) {
                if (!word.isEmpty() && word.length() > 1) {
                    wordCount.put(word, wordCount.getOrDefault(word, 0) + 1);
                    uniqueWords.add(word);
                }
            }
            
            for (String word : uniqueWords) {
                docCount.put(word, docCount.getOrDefault(word, 0) + 1);
            }
        }
        
        // Calculate TF-IDF weights
        for (Map.Entry<String, Integer> entry : wordCount.entrySet()) {
            String word = entry.getKey();
            double tf = entry.getValue() / (double) totalDocs;
            double idf = Math.log((totalDocs) / (docCount.getOrDefault(word, 1) + 1.0));
            idfWeights.put(word, tf * idf);
        }
        
        // Build vocabulary sorted by importance
        vocabulary = idfWeights.entrySet().stream()
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .limit(maxFeatures)
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> 1,
                (e1, e2) -> e1,
                LinkedHashMap::new
            ));
    }
    
    public double[] textToVector(String text, int vectorSize) {
        double[] vector = new double[vectorSize];
        String[] words = text.toLowerCase().replaceAll("[^a-zA-Z\\s]", "").split("\\s+");
        
        // Create word frequency map for this text
        Map<String, Integer> wordFreq = new HashMap<>();
        for (String word : words) {
            if (!word.isEmpty() && word.length() > 1) {
                wordFreq.put(word, wordFreq.getOrDefault(word, 0) + 1);
            }
        }
        
        // Fill vector based on vocabulary
        int idx = 0;
        for (String vocabWord : vocabulary.keySet()) {
            if (idx >= vectorSize) break;
            double value = wordFreq.getOrDefault(vocabWord, 0) * 
                           idfWeights.getOrDefault(vocabWord, 1.0);
            vector[idx] = value > 0 ? Math.min(value, 1.0) : 0.0;
            idx++;
        }
        
        // Normalize the vector
        double sum = 0.0;
        for (double val : vector) sum += val * val;
        if (sum > 0) {
            double norm = Math.sqrt(sum);
            for (int i = 0; i < vector.length; i++) {
                vector[i] /= norm;
            }
        }
        
        return vector;
    }
    
    public Map<String, Integer> getVocabulary() { return vocabulary; }
    public int getMaxFeatures() { return maxFeatures; }
    public Map<String, Double> getIdfWeights() { return idfWeights; }
}