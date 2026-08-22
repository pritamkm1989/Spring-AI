package com.pkm.SpringAI.mcp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.DefaultToolDefinition;
import org.springframework.ai.tool.definition.ToolDefinition;

import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class McpToolCallback implements ToolCallback {

    private static final int MAX_RESULT_CHARS = 1000;

    private final McpSyncClient client;
    private final McpSchema.Tool mcpTool;
    private final String originalToolName;
    private final ToolDefinition toolDefinition;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public McpToolCallback(McpSyncClient client, McpSchema.Tool mcpTool, String originalToolName) {
        this.client = client;
        this.mcpTool = mcpTool;
        this.originalToolName = originalToolName;
        this.toolDefinition = buildToolDefinition(mcpTool);
    }

    @Override
    public ToolDefinition getToolDefinition() {
        return toolDefinition;
    }

    @Override
    public String call(String toolInput) {
        try {
            log.info("[McpTool] calling '{}' with input: {}", mcpTool.name(),
                    toolInput.length() > 200 ? toolInput.substring(0, 200) + "..." : toolInput);

            @SuppressWarnings("unchecked")
            Map<String, Object> args = objectMapper.readValue(toolInput, Map.class);

            McpSchema.CallToolRequest request = new McpSchema.CallToolRequest(originalToolName, args);
            McpSchema.CallToolResult result = client.callTool(request);

            String text = result.content().stream()
                    .filter(c -> c instanceof McpSchema.TextContent)
                    .map(c -> ((McpSchema.TextContent) c).text())
                    .collect(Collectors.joining("\n"));

            text = cleanResult(text, originalToolName);

            log.info("[McpTool] '{}' returned {} chars after cleaning, isError={}",
                    mcpTool.name(), text.length(), result.isError());
            return text;
        } catch (Exception e) {
            log.error("[McpTool] '{}' failed: {}", mcpTool.name(), e.getMessage(), e);
            return "McpTool '" + mcpTool.name() + "' failed: " + e.getMessage();
        }
    }

    private String cleanResult(String raw, String toolName) {
        if (raw == null || raw.isBlank()) return "No results";

        try {
            JsonNode root = objectMapper.readTree(raw);

            // Search results: {"results": [...]}
            JsonNode results = root.path("results");
            if (results.isArray() && !results.isEmpty()) {
                return cleanSearchResults(results);
            }

            // Fallback: truncate raw
            return raw.length() > MAX_RESULT_CHARS
                    ? raw.substring(0, MAX_RESULT_CHARS) + "..."
                    : raw;
        } catch (Exception e) {
            return raw.length() > MAX_RESULT_CHARS
                    ? raw.substring(0, MAX_RESULT_CHARS) + "..."
                    : raw;
        }
    }

    private String cleanSearchResults(JsonNode results) {
        StringBuilder sb = new StringBuilder();
        int count = 0;

        for (JsonNode result : results) {
            if (count >= 3) break;

            String title = result.path("title").asText("");
            String url = result.path("url").asText("");
            JsonNode excerpts = result.path("excerpts");

            if (title.isBlank() && (excerpts == null || !excerpts.isArray() || excerpts.isEmpty())) {
                continue;
            }

            sb.append(title).append(": ");

            if (excerpts.isArray() && !excerpts.isEmpty()) {
                String text = excerpts.get(0).asText();
                text = text.replaceAll("\\[([^\\]]*)\\]\\([^)]*\\)", "$1");
                text = text.replaceAll("&amp;", "&").replaceAll("&#x27;", "'");
                text = text.replaceAll("\\n+", " ").replaceAll("\\s+", " ").trim();
                if (text.length() > 150) {
                    text = text.substring(0, 150) + "...";
                }
                sb.append(text);
            }

            sb.append("\n");
            count++;
        }

        String cleaned = sb.toString();
        return cleaned.isEmpty() ? "No relevant results found" : cleaned;
    }

    private ToolDefinition buildToolDefinition(McpSchema.Tool tool) {
        String inputSchemaJson;
        try {
            inputSchemaJson = objectMapper.writeValueAsString(tool.inputSchema());
        } catch (Exception e) {
            log.warn("Failed to serialize inputSchema for tool '{}', using empty object", tool.name());
            inputSchemaJson = "{\"type\":\"object\"}";
        }

        return DefaultToolDefinition.builder()
                .name(tool.name())
                .description(tool.description() != null ? tool.description() : "MCP tool: " + tool.name())
                .inputSchema(inputSchemaJson)
                .build();
    }
}
