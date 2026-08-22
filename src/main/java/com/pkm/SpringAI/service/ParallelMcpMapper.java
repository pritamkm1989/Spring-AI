package com.pkm.SpringAI.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ParallelMcpMapper {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String extractSearchSummary(String rawJson) {
        if (rawJson == null || rawJson.isBlank()) {
            return "No search results";
        }

        try {
            JsonNode root = objectMapper.readTree(rawJson);

            // Try "results" array (Parallel Search format)
            JsonNode results = root.path("results");
            if (results.isArray() && !results.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (JsonNode result : results) {
                    String title = result.path("title").asText("No title");
                    String url = result.path("url").asText("");
                    JsonNode excerpts = result.path("excerpts");

                    sb.append("## ").append(title).append("\n");
                    sb.append("URL: ").append(url).append("\n\n");

                    if (excerpts.isArray()) {
                        for (JsonNode excerpt : excerpts) {
                            String text = excerpt.asText();
                            if (text.length() > 500) {
                                text = text.substring(0, 500) + "...";
                            }
                            sb.append(text).append("\n\n");
                        }
                    }
                }
                return sb.toString();
            }

            // Try "content" array (generic MCP format)
            JsonNode content = root.path("content");
            if (content.isArray() && !content.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (JsonNode item : content) {
                    String text = item.path("text").asText("");
                    if (!text.isEmpty()) {
                        sb.append(text).append("\n\n");
                    }
                }
                if (!sb.isEmpty()) {
                    return sb.toString();
                }
            }

            // Try "answer" or "result" fields
            String answer = root.path("answer").asText(null);
            if (answer != null) return answer;

            String result = root.path("result").asText(null);
            if (result != null) return result;

            // Fallback: return raw with truncation
            return rawJson.length() > 3000 ? rawJson.substring(0, 3000) + "..." : rawJson;

        } catch (Exception e) {
            log.warn("Failed to parse search results as JSON, returning raw text", e);
            return rawJson.length() > 3000 ? rawJson.substring(0, 3000) + "..." : rawJson;
        }
    }
}
