package com.pkm.SpringAI.agent;

import com.pkm.SpringAI.mcp.McpToolCallback;
import com.pkm.SpringAI.tool.base.AgenticTool;
import com.pkm.SpringAI.tool.base.RateLimitedToolCallback;
import com.pkm.SpringAI.tool.base.ToolRateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class AgentToolExecutor {

    String prompt = new String("""
            You are a helpful assistant with access to tools.
            
            FINAL RESPONSE RULE:
            After using any tools, return ONLY a valid JSON object:
            {
              "questions": "<original user question>",
              "answer": "<concise answer based on tool results>",
              "listOfToolsUsed": ["<tool names>"]
            }
            
            Rules:
            - Do not include markdown or ```json.
            - Answer based only on tool results, not memory.
            """);

    private final ChatClient chatClient;
    private final ToolRateLimiter rateLimiter;

    public AgentToolExecutor(@Qualifier("qwen2ChatModel") ChatModel chatModel,
                             List<AgenticTool> tools,
                             ToolRateLimiter rateLimiter,
                             ObjectProvider<List<McpToolCallback>> mcpToolCallbacksProvider) {
        this.rateLimiter = rateLimiter;

        Map<String, AgenticTool> toolMap = new HashMap<>();
        for (AgenticTool tool : tools) {
            ToolCallback[] cbs = ToolCallbacks.from(tool);
            for (ToolCallback cb : cbs) {
                toolMap.put(cb.getToolDefinition().name(), tool);
            }
        }

        List<ToolCallback> allCallbacks = new ArrayList<>();

        // Wrap @Tool annotated methods with rate limiting
        for (AgenticTool tool : tools) {
            ToolCallback[] cbs = ToolCallbacks.from(tool);
            for (ToolCallback cb : cbs) {
                allCallbacks.add(new RateLimitedToolCallback(cb, rateLimiter, tool));
            }
        }

        // Add MCP tool callbacks with rate limiting
        List<McpToolCallback> mcpCallbacks = mcpToolCallbacksProvider.getIfAvailable();
        if (mcpCallbacks != null && !mcpCallbacks.isEmpty()) {
            log.info("[AgentToolExecutor] adding {} MCP tool callbacks", mcpCallbacks.size());
            AgenticTool mcpToolStub = new AgenticTool() {
                @Override
                public int getMaxCallsPerQuestion() {
                    return 5;
                }
            };
            for (McpToolCallback mcpCb : mcpCallbacks) {
                allCallbacks.add(new RateLimitedToolCallback(mcpCb, rateLimiter, mcpToolStub));
                log.info("[AgentToolExecutor]   + MCP tool: {}", mcpCb.getToolDefinition().name());
            }
        }

        log.info("[AgentToolExecutor] total registered tool callbacks: {}", allCallbacks.size());

        this.chatClient = ChatClient.builder(chatModel)
                .defaultToolCallbacks(allCallbacks.toArray(ToolCallback[]::new))
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
