package com.aiml.voice.audio;

import javax.sound.sampled.*;
import java.io.*;
import java.nio.file.*;

public class AudioRecorder {
    private static final int SAMPLE_RATE = 16000;
    private static final int SAMPLE_SIZE_IN_BITS = 16;
    private static final int CHANNELS = 1;
    private static final boolean SIGNED = true;
    private static final boolean BIG_ENDIAN = false;

    private AudioFormat format;
    private TargetDataLine line;
    private boolean isRecording;
    private ByteArrayOutputStream audioBuffer;

    public AudioRecorder() {
        format = new AudioFormat(SAMPLE_RATE, SAMPLE_SIZE_IN_BITS, CHANNELS, SIGNED, BIG_ENDIAN);
        audioBuffer = new ByteArrayOutputStream();
    }

    public void startRecording() throws LineUnavailableException {
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        line = (TargetDataLine) AudioSystem.getLine(info);
        line.open(format);
        line.start();
        isRecording = true;
        audioBuffer.reset();
        
        System.out.println("🎤 Recording...");
        
        Thread recordingThread = new Thread(() -> {
            byte[] buffer = new byte[4096];
            while (isRecording) {
                int bytesRead = line.read(buffer, 0, buffer.length);
                if (bytesRead > 0) {
                    audioBuffer.write(buffer, 0, bytesRead);
                }
            }
        });
        recordingThread.start();
    }

    public void stopRecording() {
        if (line != null && isRecording) {
            isRecording = false;
            line.stop();
            line.close();
            System.out.println("⏹️ Recording stopped.");
        }
    }

    public void saveToFile(String fileName) throws IOException {
        byte[] audioData = audioBuffer.toByteArray();
        
        // Create WAV file
        try (ByteArrayInputStream bais = new ByteArrayInputStream(audioData);
             AudioInputStream ais = new AudioInputStream(bais, format, audioData.length / format.getFrameSize())) {
            AudioSystem.write(ais, AudioFileFormat.Type.WAVE, new File(fileName));
        }
        System.out.println("✅ Audio saved to: " + fileName);
    }

    public byte[] getAudioData() {
        return audioBuffer.toByteArray();
    }

    public void recordForDuration(String fileName, int seconds) throws Exception {
        startRecording();
        Thread.sleep(seconds * 1000);
        stopRecording();
        saveToFile(fileName);
    }
}