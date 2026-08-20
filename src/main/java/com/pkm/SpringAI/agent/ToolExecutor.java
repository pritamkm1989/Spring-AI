package com.pkm.SpringAI.agent;

import com.pkm.SpringAI.tool.base.AgenticTool;
import com.pkm.SpringAI.tool.base.ToolRateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ToolExecutor {

    private static final Map<String, String> ACTION_TO_TOOL = Map.of(
            "SEARCH_KB", "searchKnowledgeBase",
            "SEARCH_KB_CATEGORY", "searchKnowledgeBaseByCategory",
            "WEB_SEARCH", "webSearch",
            "GET_COORDINATES", "getCoordinatesForCity",
            "GET_WEATHER", "getCurrentWeather",
            "GET_CUSTOMER", "getCustomerByEmail",
            "WRITE_FILE", "writeToFile"
    );

    private final Map<String, ToolCallback> toolCallbackMap;
    private final Map<String, AgenticTool> toolBeanMap;
    private final ToolRateLimiter rateLimiter;

    public ToolExecutor(List<AgenticTool> tools, ToolRateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
        this.toolCallbackMap = new HashMap<>();
        this.toolBeanMap = new HashMap<>();

        for (AgenticTool tool : tools) {
            ToolCallback[] cbs = ToolCallbacks.from(tool);
            for (ToolCallback cb : cbs) {
                String name = cb.getToolDefinition().name();
                toolCallbackMap.put(name, cb);
                toolBeanMap.put(name, tool);
            }
        }

        log.info("[ToolExecutor] registered {} tool callbacks: {}", toolCallbackMap.size(),
                toolCallbackMap.keySet());
    }

    public String execute(ToolClassification classification) {
        String action = classification.getAction();
        String toolName = ACTION_TO_TOOL.get(action);

        if (toolName == null) {
            log.warn("[ToolExecutor] unknown action: {}", action);
            return "Error: unknown action " + action;
        }

        ToolCallback callback = toolCallbackMap.get(toolName);
        if (callback == null) {
            log.warn("[ToolExecutor] no callback for tool: {}", toolName);
            return "Error: tool not found " + toolName;
        }

        AgenticTool tool = toolBeanMap.get(toolName);
        if (tool != null && !rateLimiter.allow(tool, toolName)) {
            log.warn("[ToolExecutor] rate limit reached for {}", toolName);
            return "Rate limit reached for " + toolName;
        }

        String inputJson = toJson(classification.getInput());
        log.info("[ToolExecutor] calling {} ({}) with params: {}", action, toolName, inputJson);

        try {
            String result = callback.call(inputJson);
            log.info("[ToolExecutor] {} returned {} chars", toolName, result.length());
            return result;
        } catch (Exception e) {
            log.error("[ToolExecutor] {} failed: {}", toolName, e.getMessage(), e);
            return "Error calling " + toolName + ": " + e.getMessage();
        }
    }

    private String toJson(Map<String, String> params) {
        if (params == null || params.isEmpty()) return "{}";
        return params.entrySet().stream()
                .filter(e -> e.getValue() != null && !e.getValue().isBlank())
                .map(e -> "\"" + e.getKey() + "\": \"" + escapeJson(e.getValue()) + "\"")
                .collect(Collectors.joining(", ", "{", "}"));
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
