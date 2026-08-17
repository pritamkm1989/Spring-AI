package com.pkm.SpringAI.agent;

import com.pkm.SpringAI.tool.base.AgenticTool;
import com.pkm.SpringAI.tool.base.RateLimitedToolCallback;
import com.pkm.SpringAI.tool.base.ToolRateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class AgentToolExecutor {

    String prompt = new String("""
            You are an assistant with access to these tools:
            - searchKnowledgeBase(query): general search across all internal docs
            - searchKnowledgeBaseByCategory(query, category): search a specific category
            - webSearch(query): search the web for info not in the knowledge base
            - getCustomerByEmail(email): account lookups
            - getCurrentWeather, getCoordinatesForCity: weather lookups
            - writeToFile, appendToFile: save content to disk
            
            FINAL RESPONSE RULE:
            
            After you have completed all required tool calls, return ONLY a valid JSON object.
            
            The JSON must have exactly these fields:
            
            {
              "questions": "<original user question>",
              "answer": "<final answer>",
              "listOfToolsUsed": ["<tool1>", "<tool2>"]
            }
            
            Rules:
            - questions must contain the original user question.
            - answer must contain the final answer to the user.
            - listOfToolsUsed must contain the names of tools actually used.
            - Do not include markdown.
            - Do not include ```json.
            - Do not include any text outside the JSON.

            RULES:
            1. For questions about internal docs, policies, products, HR or company
               information, FIRST call searchKnowledgeBase (or searchKnowledgeBaseByCategory)
               to retrieve relevant context, then answer based on it. Never answer from memory.
            2. Rewrite vague or unclear questions into clear, specific search queries
               before calling a search tool.
            3. Only use webSearch when the knowledge base returns no relevant results.
            4. Use weather, account or file tools only when the user explicitly asks
               about weather, customer data, or saving content to a file.
            5. If a tool call returns no results, try once more with a different, more
               specific query before giving up.
            6. If you still have no relevant information, say so clearly instead of
               guessing.
            """);

    private final ChatClient chatClient;
    private final ToolRateLimiter rateLimiter;

    public AgentToolExecutor(@Qualifier("qwen2ChatModel") ChatModel chatModel,
                             List<AgenticTool> tools,
                             ToolRateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;

        Map<String, AgenticTool> toolMap = new HashMap<>();
        for (AgenticTool tool : tools) {
            ToolCallback[] cbs = ToolCallbacks.from(tool);
            for (ToolCallback cb : cbs) {
                toolMap.put(cb.getToolDefinition().name(), tool);
            }
        }

        ToolCallback[] wrapped = tools.stream()
                .flatMap(tool -> Arrays.stream(ToolCallbacks.from(tool)))
                .map(cb -> (ToolCallback) new RateLimitedToolCallback(
                        cb, rateLimiter, toolMap.get(cb.getToolDefinition().name())))
                .toArray(ToolCallback[]::new);

        log.info("[AgentToolExecutor] registered {} rate-limited tool callbacks", wrapped.length);

        this.chatClient = ChatClient.builder(chatModel)
                .defaultToolCallbacks(wrapped)
                .defaultSystem(prompt)
                .build();
    }

    public String execute(String question) {
        rateLimiter.reset();
        ChatResponse response = chatClient.prompt()
                .user(question)
                .call()
                .chatResponse();
        log.info("[AgentToolExecutor] usage={}", response.getMetadata().getUsage());
        return response.getResult().getOutput().getText();
    }
}
