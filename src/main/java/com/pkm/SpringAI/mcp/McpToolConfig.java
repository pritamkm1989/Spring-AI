package com.pkm.SpringAI.mcp;

import com.pkm.SpringAI.config.McpServerConfig;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Slf4j
@Configuration
public class McpToolConfig {


    @Bean
    public List<McpToolCallback> mcpToolCallbacks(McpServerConfig properties) {
        List<McpToolCallback> callbacks = new ArrayList<>();

        if (properties.getServers() == null || properties.getServers().isEmpty()) {
            log.warn("[McpToolConfig] no MCP servers configured under mcp.servers.list");
            return callbacks;
        }

        for (McpServerConfig.ServerConfig server : properties.getServers()) {
            try {
                callbacks.addAll(connectServer(server));
            } catch (Exception e) {
                log.error("[McpToolConfig] failed to connect to MCP server '{}': {}",
                        server.getName(), e.getMessage());
            }
        }

        log.info("[McpToolConfig] total MCP tool callbacks registered: {}", callbacks.size());
        return callbacks;
    }

    private List<McpToolCallback> connectServer(McpServerConfig.ServerConfig server) {
        List<McpToolCallback> callbacks = new ArrayList<>();

        McpSyncClient client = buildClient(server);

        log.info("[McpToolConfig] connected to '{}' [{}], listing tools...", server.getName(), server.getTransport());
        McpSchema.ListToolsResult tools = client.listTools();
        log.info("[McpToolConfig] '{}' returned {} tools", server.getName(), tools.tools().size());

        for (McpSchema.Tool tool : tools.tools()) {
            String toolName = server.getPrefix() != null && !server.getPrefix().isBlank()
                    ? server.getPrefix() + "__" + tool.name()
                    : tool.name();

            McpSchema.Tool prefixedTool = McpSchema.Tool.builder()
                    .name(toolName)
                    .title(tool.title())
                    .description(tool.description())
                    .inputSchema(tool.inputSchema())
                    .outputSchema(tool.outputSchema())
                    .annotations(tool.annotations())
                    .meta(tool.meta())
                    .build();

            log.info("[McpToolConfig]   -> {} : {}", toolName,
                    tool.description() != null
                            ? tool.description().substring(0, Math.min(80, tool.description().length()))
                            : "no desc");
            callbacks.add(new McpToolCallback(client, prefixedTool, tool.name()));
        }

        return callbacks;
    }

    private McpSyncClient buildClient(McpServerConfig.ServerConfig server) {
        String transport = server.getTransport() != null ? server.getTransport() : "streamable";

        if ("sse".equalsIgnoreCase(transport)) {
            return buildSseClient(server);
        } else {
            return buildStreamableClient(server);
        }
    }

    private String buildAuthHeader(McpServerConfig.ServerConfig server) {
        if (server.getUsername() != null && !server.getUsername().isBlank()
                && server.getPassword() != null) {
            String credentials = server.getUsername() + ":" + server.getPassword();
            String encoded = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
            return "Basic " + encoded;
        }
        if (server.getApiKey() != null && !server.getApiKey().isBlank()) {
            return "Bearer " + server.getApiKey();
        }
        return null;
    }

    private McpSyncClient buildSseClient(McpServerConfig.ServerConfig server) {
        String authHeader = buildAuthHeader(server);

        var transport = HttpClientSseClientTransport.builder(server.getBaseUri())
                .sseEndpoint(server.getSseEndpoint())
                .connectTimeout(Duration.ofSeconds(server.getConnectTimeout()))
                .httpRequestCustomizer((builder, method, endpoint, body, context) -> {
                    if (authHeader != null) {
                        builder.header("Authorization", authHeader);
                    }
                })
                .build();

        return McpClient.sync(transport)
                .clientInfo(new McpSchema.Implementation(
                        server.getName() + "-agent", server.getClientVersion()))
                .requestTimeout(Duration.ofSeconds(server.getRequestTimeout()))
                .build();
    }

    private McpSyncClient buildStreamableClient(McpServerConfig.ServerConfig server) {
        String authHeader = buildAuthHeader(server);

        var transport = HttpClientStreamableHttpTransport.builder(server.getBaseUri())
                .endpoint(server.getEndpoint())
                .openConnectionOnStartup(false)
                .connectTimeout(Duration.ofSeconds(server.getConnectTimeout()))
                .asyncHttpRequestCustomizer((builder, method, endpoint, body, context) -> {
                    if (authHeader != null) {
                        builder.header("Authorization", authHeader);
                    }
                    return reactor.core.publisher.Mono.just(builder);
                })
                .build();

        return McpClient.sync(transport)
                .clientInfo(new McpSchema.Implementation(
                        server.getName() + "-agent", server.getClientVersion()))
                .requestTimeout(Duration.ofSeconds(server.getRequestTimeout()))
                .build();
    }
}
