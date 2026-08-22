package com.pkm.SpringAI.graph;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ToolClassification {

    private String action;
    private Map<String, String> input;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public boolean isFinalAnswer() {
        return "FINAL_ANSWER".equals(action);
    }

    public String getToolName() {
        return action;
    }

    public static ToolClassification parse(String json) {
        try {
            String cleaned = json.strip();
            if (cleaned.startsWith("```")) {
                cleaned = cleaned.replaceAll("^```(?:json)?\\s*", "").replaceAll("```\\s*$", "");
            }
            return MAPPER.readValue(cleaned, ToolClassification.class);
        } catch (Exception e) {
            return new ToolClassification("FINAL_ANSWER", Map.of());
        }
    }
}
