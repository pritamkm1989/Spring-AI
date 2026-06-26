package com.pkm.SpringAI.config;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.List;

@Configuration
@Slf4j
public class OllamaEmbeddingInterceptor {


    // ===== EMBEDDING LOGGING =====
    @Bean
    @Primary
    public EmbeddingModel loggingEmbeddingModel(@Qualifier("ollamaEmbeddingModel") EmbeddingModel delegate) {
        return new EmbeddingModel() {
            @Override
            public EmbeddingResponse call(EmbeddingRequest request) {

                log.info(">>> OLLAMA EMBEDDING REQUEST");
                request.getInstructions().forEach(text ->
                        log.info("  Text: {}", text)
                );

                long start = System.currentTimeMillis();
                EmbeddingResponse response = delegate.call(request);
                long ms = System.currentTimeMillis() - start;

                log.info("<<< OLLAMA EMBEDDING RESPONSE ({}ms)", ms);
                log.info("  Vectors count : {}", response.getResults().size());
                log.info("  Dimensions    : {}", response.getResults().get(0).getOutput().length);
                log.info("==============================");
                return response;
            }

            @Override
            public float[] embed(Document document) {
                log.info(">>> OLLAMA EMBED DOCUMENT: {}", document.getText());
                float[] result = delegate.embed(document);
                log.info("<<< EMBED DONE - Dimensions: {}", result.length);
                return result;
            }

            @Override
            public float[] embed(String text) {
                log.info(">>> OLLAMA EMBED TEXT: {}", text);
                float[] result = delegate.embed(text);
                log.info("<<< EMBED DONE - Dimensions: {}", result.length);
                return result;
            }

            @Override
            public List<float[]> embed(List<String> texts) {
                log.info(">>> OLLAMA EMBED BATCH - Count: {}", texts.size());
                List<float[]> result = delegate.embed(texts);
                log.info("<<< EMBED BATCH DONE");
                return result;
            }

            @Override
            public int dimensions() {
                return delegate.dimensions();
            }
        };
    }
}