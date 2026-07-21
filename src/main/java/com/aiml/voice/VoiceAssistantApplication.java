package com.aiml.voice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = {
    org.springframework.ai.autoconfigure.openai.OpenAiAutoConfiguration.class
})
@EnableScheduling
public class VoiceAssistantApplication {
    public static void main(String[] args) {
        SpringApplication.run(VoiceAssistantApplication.class, args);
    }
}