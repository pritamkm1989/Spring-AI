package com.pkm.SpringAI.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class MultiModelConfig {

    @Value("${spring.ai.ollama.base-url}")
    private String olamaApiUrl;


    @Bean
    @Primary
    public OllamaApi  getOllamaApi() {
        return OllamaApi.builder()
                .baseUrl(olamaApiUrl)
                .build();
    }

    @Bean
    public OllamaChatModel phi3ChatModel(OllamaApi ollamaApi) {
        return OllamaChatModel.builder()
                .ollamaApi(ollamaApi)
                .defaultOptions(OllamaChatOptions.builder()
                        .model("phi3.5:3.8b")     // ← different model here
                        .temperature(0d)
                        .topP(0.9)
                        .topK(40)
                        .numPredict(200)
                        .numCtx(4096)
                        .repeatPenalty(1.1)
                        .build())
                .build();
    }

    @Bean
    @Primary
    public OllamaChatModel gemma2ChatModel(OllamaApi ollamaApi) {

        return OllamaChatModel.builder()
                .ollamaApi(ollamaApi)
                .defaultOptions(OllamaChatOptions.builder()
                        .model("gemma2:2b")     // ← different model here
                        .temperature(0d)
                        .topP(0.9)
                        .topK(40)
                        .numPredict(200)
                        .numCtx(4096)
                        .repeatPenalty(1.1)
                        .build())
                .build();
    }

    @Bean
    public ChatClient phi3ChatChatClient(@Qualifier("phi3ChatModel") OllamaChatModel phi3ChatModel ) {
        return ChatClient.builder(phi3ChatModel).build();
    }

    @Bean
    @Primary
    public ChatClient gemma2ChatChatClient(@Qualifier("gemma2ChatModel") OllamaChatModel gemma2ChatModel ) {
        return ChatClient.builder(gemma2ChatModel).build();
    }
}
