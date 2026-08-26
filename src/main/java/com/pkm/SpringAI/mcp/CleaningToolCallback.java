package com.pkm.SpringAI.mcp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;

@Slf4j
public class CleaningToolCallback implements ToolCallback {

    private static final int MAX_RESULT_CHARS = 1000;
    private final ToolCallback delegate;
    private final String originalToolName;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CleaningToolCallback(ToolCallback delegate, String originalToolName) {
        this.delegate = delegate;
        this.originalToolName = originalToolName;
    }

    @Override
    public ToolDefinition getToolDefinition() {
        return delegate.getToolDefinition();
    }

    @Override
    public String call(String toolInput) {
        String result = delegate.call(toolInput);
        return cleanResult(result, originalToolName);
    }

    private String cleanResult(String raw, String toolName) {
        if (raw == null || raw.isBlank()) return "No results";

        try {
            JsonNode root = objectMapper.readTree(raw);

            JsonNode results = root.path("results");
            if (results.isArray() && !results.isEmpty()) {
                return cleanSearchResults(results);
            }

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
}
