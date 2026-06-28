package com.pkm.SpringAI.service.impl;

import com.pkm.SpringAI.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Service
@Slf4j
public class ChatServiceImpl implements ChatService {

    @Autowired
    private  ChatClient chatClient;

    @Override
    public Flux<String> chatPrompt(String prompt) {
        log.info("invoke service");


        String systemText = """
    You must follow these strict rules for your formatting:
    1. Write your entire answer using plain paragraphs only.
    2. Do not use bullet points, numbered lists, markdown bold, or headers.
    3. Separate different ideas using standard paragraph breaks.
    """;

        String userTemplateText = "Explain {topic} like I am 10 years old.";

        SystemMessage systemMessage = new SystemMessage(systemText);

// Render the template text directly, then wrap as UserMessage
        PromptTemplate userTemplate = new PromptTemplate(userTemplateText);
        String renderedUserText = userTemplate.render(Map.of("topic", prompt));

        UserMessage userMessage = new UserMessage(renderedUserText);

        Prompt fullPrompt = new Prompt(List.of(systemMessage, userMessage));

        Flux<String> tokenStream = this.chatClient.prompt(fullPrompt)
                .stream()
                .content();



        // 2. Thread-safe buffer container to hold text pieces across chunk boundaries
        AtomicReference<StringBuilder> buffer = new AtomicReference<>(new StringBuilder());

        return tokenStream
                .concatMap(chunk -> {
                    StringBuilder sb = buffer.get();
                    sb.append(chunk);
                    String currentText = sb.toString();

                    // Split text at sentence boundaries (. ! ?) keeping the punctuation mark
                    String[] segments = currentText.split("(?<=[.!?])\\s+");

                    // Check if the last segment is still incomplete (lacks trailing punctuation)
                    if (segments.length > 0 && !segments[segments.length - 1].matches(".*[.!?]$")) {
                        // Store the incomplete sentence back in the buffer for the next chunk
                        buffer.set(new StringBuilder(segments[segments.length - 1]));
                        // Pass along only the fully completed sentences
                        return Flux.fromArray(segments).take(segments.length - 1);
                    } else {
                        // All segments are fully formed sentences
                        buffer.set(new StringBuilder());
                        return Flux.fromArray(segments);
                    }
                })
                // Flush out any remaining trailing text when the AI finishes its generation
                .concatWith(Flux.defer(() -> {
                    String remaining = buffer.get().toString().trim();
                    return remaining.isEmpty() ? Flux.empty() : Flux.just(remaining);
                }));
    }
}
