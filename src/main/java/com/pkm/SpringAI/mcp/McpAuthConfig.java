package com.pkm.SpringAI.mcp;

import io.modelcontextprotocol.client.transport.customizer.McpSyncHttpClientRequestCustomizer;
import io.modelcontextprotocol.common.McpTransportContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
@Configuration
public class McpAuthConfig {

    @Value("${mcp.auth.parallel-search.api-key:}")
    private String parallelSearchApiKey;

    @Value("${mcp.auth.local-server.username:}")
    private String localServerUsername;

    @Value("${mcp.auth.local-server.password:}")
    private String localServerPassword;

    @Bean
    public McpSyncHttpClientRequestCustomizer mcpAuthCustomizer() {
        return (HttpRequest.Builder builder, String method, URI endpoint, String body, McpTransportContext context) -> {
            String url = endpoint.toString();

            if (url.contains("search.parallel.ai") && parallelSearchApiKey != null && !parallelSearchApiKey.isBlank()) {
                builder.header("Authorization", "Bearer " + parallelSearchApiKey);
            } else if (url.contains("localhost:8090")) {
                String authHeader = buildBasicAuthHeader(localServerUsername, localServerPassword);
                if (authHeader != null) {
                    builder.header("Authorization", authHeader);
                }
            }
        };
    }

    private String buildBasicAuthHeader(String username, String password) {
        if (username != null && !username.isBlank() && password != null) {
            String credentials = username + ":" + password;
            String encoded = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
            return "Basic " + encoded;
        }
        return null;
    }
}
