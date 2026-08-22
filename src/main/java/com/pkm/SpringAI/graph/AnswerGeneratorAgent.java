package com.pkm.SpringAI.graph;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class AnswerGeneratorAgent {

    private static final String ANSWER_PROMPT = """
            You are an assistant that formats answers based on tool results.

            Given the original user question and the step-by-step observations, produce a final answer.

            Return ONLY a valid JSON object — no markdown, no explanation, no ```json fencing:

            {
              "questions": "<original user question>",
              "answer": "<final answer to the user>",
              "listOfToolsUsed": ["<tool names that were actually called>"]
            }

            Rules:
            - answer must be a clear, complete response to the user's question.
            - listOfToolsUsed must contain only tools that were actually called.
            - If a tool returned an error or no results, mention that in the answer.
            - Do not include any text outside the JSON.
            """;

    private final ChatClient chatClient;

    public AnswerGeneratorAgent(@Qualifier("qwen2ChatModel") ChatModel chatModel) {
        this.chatClient = ChatClient.builder(chatModel)
                .defaultSystem(ANSWER_PROMPT)
                .build();
    }

    public String generate(String question, List<String> observations, List<String> toolsCalled) {
        StringBuilder context = new StringBuilder();
        context.append("Original question: ").append(question).append("\n\n");
        context.append("Step-by-step observations:\n");

        for (int i = 0; i < observations.size(); i++) {
            context.append("Step ").append(i + 1).append(": ").append(observations.get(i)).append("\n");
        }

        context.append("\nTools called: ").append(String.join(", ", toolsCalled));

        log.info("[AnswerGenerator] generating answer for {} observations", observations.size());
        String response = chatClient.prompt()
                .user(context.toString())
                .call()
                .content();

        log.info("[AnswerGenerator] response length: {} chars", response.length());
        return response;
    }
}
