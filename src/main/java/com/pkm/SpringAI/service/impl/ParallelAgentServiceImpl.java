package com.pkm.SpringAI.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pkm.SpringAI.service.ParallelAgentService;
import com.pkm.SpringAI.service.ParallelMcpMapper;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ParallelAgentServiceImpl implements ParallelAgentService {

    private final ChatModel chatModel;
    private final McpSyncClient parallelSearchClient;

    private final ParallelMcpMapper parallelMcpMapper;

    public ParallelAgentServiceImpl(
            @Qualifier("qwen2ChatModel") ChatModel chatModel,
            McpSyncClient parallelSearchClient,ParallelMcpMapper parallelMcpMapper) {
        this.chatModel = chatModel;
        this.parallelSearchClient = parallelSearchClient;
        this.parallelMcpMapper = parallelMcpMapper;
    }

    @Override
    public Map<String, String> invokeParallel(String question) {
        String data = invokeMcpSearch(question);

        return Map.of(
                "mcp-search", data
        );
    }

    private String invokeMcpSearch(String question) {
        try {
            log.info("Listing MCP tools...");
            McpSchema.ListToolsResult tools = parallelSearchClient.listTools();
            log.info("Found {} MCP tools: {}", tools.tools().size(),
                    tools.tools().stream().map(McpSchema.Tool::name).collect(Collectors.joining(", ")));

            if (tools.tools().isEmpty()) {
                return "No MCP tools available";
            }

            String toolName = tools.tools().stream()
                    .filter(t -> t.name().equals("web_search"))
                    .findFirst()
                    .map(McpSchema.Tool::name)
                    .orElse(tools.tools().get(0).name());

            McpSchema.CallToolRequest request = new McpSchema.CallToolRequest(
                    toolName,
                    Map.of(
                            "search_queries", List.of(question),
                            "objective", "Find information about: " + question
                    )
            );

            log.info("Calling MCP tool '{}' with question: {}", toolName, question);
            McpSchema.CallToolResult result = parallelSearchClient.callTool(request);

            log.info("MCP result has {} content items, isError={}", result.content().size(), result.isError());

            String text = result.content().stream()
                    .filter(c -> c instanceof McpSchema.TextContent)
                    .map(c -> ((McpSchema.TextContent) c).text())
                    .collect(Collectors.joining("\n"));

            log.info("MCP raw response length: {}, first 1000 chars:\n{}", text.length(),
                    text.substring(0, Math.min(1000, text.length())));

            return parallelMcpMapper.extractSearchSummary(text);
        } catch (Exception e) {
            log.error("MCP search failed: {}", e.getMessage(), e);
            return "MCP error: " + e.getClass().getSimpleName() + ": " + e.getMessage();
        }
    }




}
