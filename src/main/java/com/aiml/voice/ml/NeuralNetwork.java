package com.aiml.voice.ml;

import java.io.*;
import java.util.*;

public class NeuralNetwork implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private double[][] weights1;
    private double[][] weights2;
    private double[] bias1;
    private double[] bias2;
    private int inputSize;
    private int hiddenSize;
    private int outputSize;
    private double learningRate = 0.01;
    
    public NeuralNetwork(int inputSize, int hiddenSize, int outputSize) {
        this.inputSize = inputSize;
        this.hiddenSize = hiddenSize;
        this.outputSize = outputSize;
        
        Random rand = new Random(42); // Fixed seed for reproducibility
        
        // Xavier initialization for better convergence
        weights1 = new double[inputSize][hiddenSize];
        weights2 = new double[hiddenSize][outputSize];
        bias1 = new double[hiddenSize];
        bias2 = new double[outputSize];
        
        double w1Scale = Math.sqrt(2.0 / inputSize);
        double w2Scale = Math.sqrt(2.0 / hiddenSize);
        
        for (int i = 0; i < inputSize; i++) {
            for (int j = 0; j < hiddenSize; j++) {
                weights1[i][j] = rand.nextGaussian() * w1Scale;
            }
        }
        for (int i = 0; i < hiddenSize; i++) {
            for (int j = 0; j < outputSize; j++) {
                weights2[i][j] = rand.nextGaussian() * w2Scale;
            }
        }
    }

    public double[] forward(double[] input) {
        // Hidden layer with ReLU
        double[] hidden = new double[hiddenSize];
        for (int i = 0; i < hiddenSize; i++) {
            double sum = bias1[i];
            for (int j = 0; j < inputSize; j++) {
                sum += input[j] * weights1[j][i];
            }
            hidden[i] = Math.max(0, sum); // ReLU
        }
        
        // Output layer with Softmax
        double[] output = new double[outputSize];
        double maxVal = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < outputSize; i++) {
            double sum = bias2[i];
            for (int j = 0; j < hiddenSize; j++) {
                sum += hidden[j] * weights2[j][i];
            }
            output[i] = sum;
            if (sum > maxVal) maxVal = sum;
        }
        
        // Softmax for probability distribution
        double sumExp = 0;
        for (int i = 0; i < outputSize; i++) {
            output[i] = Math.exp(output[i] - maxVal);
            sumExp += output[i];
        }
        for (int i = 0; i < outputSize; i++) {
            output[i] /= sumExp;
        }
        
        return output;
    }

    public void train(double[][] inputs, double[][] targets, int epochs, double lr) {
        this.learningRate = lr;
        System.out.println("Training Neural Network...");
        System.out.println("Samples: " + inputs.length + ", Input size: " + inputSize);
        System.out.println("Epochs: " + epochs + ", Learning rate: " + lr);
        
        for (int epoch = 0; epoch < epochs; epoch++) {
            double totalLoss = 0;
            
            // Shuffle data
            List<Integer> indices = new ArrayList<>();
            for (int i = 0; i < inputs.length; i++) indices.add(i);
            Collections.shuffle(indices);
            
            for (int idx : indices) {
                double[] input = inputs[idx];
                double[] target = targets[idx];
                
                // Forward pass
                double[] hidden = new double[hiddenSize];
                for (int i = 0; i < hiddenSize; i++) {
                    double sum = bias1[i];
                    for (int j = 0; j < inputSize; j++) {
                        sum += input[j] * weights1[j][i];
                    }
                    hidden[i] = Math.max(0, sum);
                }
                
                double[] output = new double[outputSize];
                double maxVal = Double.NEGATIVE_INFINITY;
                for (int i = 0; i < outputSize; i++) {
                    double sum = bias2[i];
                    for (int j = 0; j < hiddenSize; j++) {
                        sum += hidden[j] * weights2[j][i];
                    }
                    output[i] = sum;
                    if (sum > maxVal) maxVal = sum;
                }
                
                double sumExp = 0;
                for (int i = 0; i < outputSize; i++) {
                    output[i] = Math.exp(output[i] - maxVal);
                    sumExp += output[i];
                }
                for (int i = 0; i < outputSize; i++) {
                    output[i] /= sumExp;
                }
                
                // Cross-entropy loss
                for (int i = 0; i < outputSize; i++) {
                    totalLoss -= Math.log(Math.max(output[i], 1e-10)) * target[i];
                }
                
                // Backward pass - output layer
                double[] delta2 = new double[outputSize];
                for (int i = 0; i < outputSize; i++) {
                    delta2[i] = output[i] - target[i];
                }
                
                // Backward pass - hidden layer
                double[] delta1 = new double[hiddenSize];
                for (int i = 0; i < hiddenSize; i++) {
                    double sum = 0;
                    for (int j = 0; j < outputSize; j++) {
                        sum += delta2[j] * weights2[i][j];
                    }
                    delta1[i] = (hidden[i] > 0) ? sum : 0;
                }
                
                // Update weights - output layer
                for (int i = 0; i < hiddenSize; i++) {
                    for (int j = 0; j < outputSize; j++) {
                        weights2[i][j] -= learningRate * delta2[j] * hidden[i];
                    }
                }
                for (int i = 0; i < outputSize; i++) {
                    bias2[i] -= learningRate * delta2[i];
                }
                
                // Update weights - hidden layer
                for (int i = 0; i < inputSize; i++) {
                    for (int j = 0; j < hiddenSize; j++) {
                        weights1[i][j] -= learningRate * delta1[j] * input[i];
                    }
                }
                for (int i = 0; i < hiddenSize; i++) {
                    bias1[i] -= learningRate * delta1[i];
                }
            }
            
            // Print progress every 50 epochs
            if (epoch % 50 == 0) {
                double avgLoss = totalLoss / inputs.length;
                System.out.printf("Epoch %d/%d, Avg Loss: %.4f%n", epoch, epochs, avgLoss);
            }
        }
        System.out.println("✅ Training complete!");
    }
}