package com.pkm.SpringAI.agent;

import com.pkm.SpringAI.mcp.CleaningToolCallback;
import com.pkm.SpringAI.tool.base.AgenticTool;
import com.pkm.SpringAI.tool.base.RateLimitedToolCallback;
import com.pkm.SpringAI.tool.base.ToolCallTracker;
import com.pkm.SpringAI.tool.base.ToolRateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
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
    private final ToolCallTracker toolCallTracker;

    public AgentToolExecutor(@Qualifier("qwen2ChatModel") ChatModel chatModel,
                             List<AgenticTool> tools,
                             ToolRateLimiter rateLimiter,
                             ObjectProvider<SyncMcpToolCallbackProvider> mcpProviderProvider,
                             ToolCallTracker toolCallTracker) {
        this.rateLimiter = rateLimiter;
        this.toolCallTracker = toolCallTracker;

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
                allCallbacks.add(new RateLimitedToolCallback(cb, rateLimiter, tool, toolCallTracker));
            }
        }

        // Add MCP tool callbacks from Spring AI starter with cleaning + rate limiting
        SyncMcpToolCallbackProvider mcpProvider = mcpProviderProvider.getIfAvailable();
        if (mcpProvider != null) {
            ToolCallback[] mcpCallbacks = mcpProvider.getToolCallbacks();
            if (mcpCallbacks != null && mcpCallbacks.length > 0) {
                log.info("[AgentToolExecutor] adding {} MCP tool callbacks from Spring AI starter", mcpCallbacks.length);
                AgenticTool mcpToolStub = new AgenticTool() {
                    @Override
                    public int getMaxCallsPerQuestion() {
                        return 5;
                    }
                };
                for (ToolCallback mcpCb : mcpCallbacks) {
                    ToolCallback cleanedCb = new CleaningToolCallback(mcpCb, mcpCb.getToolDefinition().name());
                    allCallbacks.add(new RateLimitedToolCallback(cleanedCb, rateLimiter, mcpToolStub, toolCallTracker));
                    log.info("[AgentToolExecutor]   + MCP tool: {}", mcpCb.getToolDefinition().name());
                }
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
        toolCallTracker.reset();
        ChatResponse response = chatClient.prompt()
                .user(question)
                .call()
                .chatResponse();
        log.info("[AgentToolExecutor] usage={}", response.getMetadata().getUsage());

        String rawAnswer = response.getResult().getOutput().getText();
        List<String> trackedTools = toolCallTracker.getToolNames();
        log.info("[AgentToolExecutor] tracked tools: {}", trackedTools);

        return mergeResponse(rawAnswer, trackedTools);
    }

    private String mergeResponse(String rawAnswer, List<String> trackedTools) {
        if (trackedTools.isEmpty()) return rawAnswer;

        try {
            var root = new com.fasterxml.jackson.databind.ObjectMapper().readTree(rawAnswer);
            var actualTools = trackedTools;

            var missing = new ArrayList<>(actualTools);
            var modelTools = root.path("listOfToolsUsed");
            if (modelTools.isArray()) {
                for (var t : modelTools) {
                    missing.remove(t.asText());
                }
            }

            if (!missing.isEmpty()) {
                var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                var node = mapper.createObjectNode();
                node.set("questions", root.path("questions"));
                node.set("answer", root.path("answer"));

                var toolsArray = mapper.createArrayNode();
                for (var t : actualTools) {
                    toolsArray.add(t);
                }
                node.set("listOfToolsUsed", toolsArray);

                String merged = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(node);
                log.info("[AgentToolExecutor] merged listOfToolsUsed: added missing {}", missing);
                return merged;
            }
        } catch (Exception e) {
            log.warn("[AgentToolExecutor] failed to merge response, returning raw: {}", e.getMessage());
        }

        return rawAnswer;
    }
}
