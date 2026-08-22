package com.pkm.SpringAI.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pkm.SpringAI.tool.base.AgenticTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
//@Component
public class WebSearchTool{ //implements AgenticTool {

    private static final String DUCKDUCKGO_URL = "https://api.duckduckgo.com/";

    private final RestClient restClient;
    private final ObjectMapper mapper = new ObjectMapper();
    private final AtomicInteger callCount = new AtomicInteger();

    @Value("${app.tools.web-search.url:}")
    private String baseUrl;

    @Value("${app.tools.web-search.api-key:}")
    private String apiKey;

    @Value("${app.tools.web-search.max-calls-per-question:2}")
    private int maxCallsPerQuestion;

    public WebSearchTool(RestClient.Builder builder) {
        this.restClient = builder.build();
    }

    /**
     * Reset the per-question call counter. Call this before each new agent request
     * so the limit applies to a single question, not across requests.
     */
    public void reset() {
        callCount.set(0);
    }

    @Tool(description = "Search the web for current information not found in the internal knowledge base")
    public String webSearch(
            @ToolParam(description = "Clear, specific web search query, e.g. 'latest AWS regions 2026'") String query) {
        log.info("[WebSearch] query={}", query);

        int used = callCount.incrementAndGet();
        if (used > maxCallsPerQuestion) {
            log.warn("[WebSearch] call limit reached ({}/{}), rejecting query: {}",
                    used, maxCallsPerQuestion, query);
            return "Web search limit reached (max " + maxCallsPerQuestion
                    + " calls per question). No web search results found for: " + query;
        }

        try {
            String url = (baseUrl != null && !baseUrl.isBlank()) ? baseUrl : DUCKDUCKGO_URL;

            String json = restClient.get()
                    .uri(url, uriBuilder -> uriBuilder
                            .queryParam("q", query)
                            .queryParam("format", "json")
                            .queryParam("no_html", "1")
                            .queryParam("skip_disambig", "1")
                            .build())
                    .retrieve()
                    .body(String.class);

            if (json == null || json.isBlank()) {
                return "No web search results found for: " + query;
            }

            JsonNode root = mapper.readTree(json);
            String abstractText = root.path("AbstractText").asText("");
            String source = root.path("AbstractSource").asText("");

            if (!abstractText.isBlank()) {
                log.info("[WebSearch] found DuckDuckGo abstract ({} chars) from {}", abstractText.length(), source);
                return abstractText;
            }

            StringBuilder sb = new StringBuilder();
            JsonNode topics = root.path("RelatedTopics");
            if (topics.isArray()) {
                for (JsonNode topic : topics) {
                    String text = topic.path("Text").asText("");
                    if (!text.isBlank()) {
                        sb.append(text).append("\n");
                    }
                    JsonNode subTopics = topic.path("Topics");
                    if (subTopics.isArray()) {
                        for (JsonNode sub : subTopics) {
                            String subText = sub.path("Text").asText("");
                            if (!subText.isBlank()) {
                                sb.append(subText).append("\n");
                            }
                        }
                    }
                }
            }

            if (!sb.isEmpty()) {
                log.info("[WebSearch] found {} related topics", sb.toString().split("\n").length);
                return sb.toString().trim();
            }

            // No abstract and no related topics → return a decisive empty result
            // instead of raw JSON. Raw JSON makes the model retry in a loop.
            log.info("[WebSearch] no useful content for query: {}", query);
            return "No web search results found for: " + query;

        } catch (Exception e) {
            log.error("[WebSearch] failed for query: {}", query, e);
            return "Web search failed: " + e.getMessage();
        }
    }
}
