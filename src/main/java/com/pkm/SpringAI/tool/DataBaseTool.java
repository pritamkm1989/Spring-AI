package com.pkm.SpringAI.tool;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@AllArgsConstructor
public class DataBaseTool implements AgenticTool {

    private final JdbcTemplate jdbcTemplate;

    @Tool(description = "Get customer details by email")
    public String getCustomerByEmail(
            @ToolParam(description = "Email address of the customer, e.g. 'jane@example.com'") String email) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT name, plan, signup_date FROM customers WHERE email = ?", email
        );
        return rows.isEmpty() ? "No customer found" : rows.get(0).toString();
    }
}
