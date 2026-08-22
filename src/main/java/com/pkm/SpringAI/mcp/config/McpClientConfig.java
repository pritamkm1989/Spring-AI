package com.pkm.SpringAI.mcp.config;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Configuration
public class McpClientConfig {

    @Value("${parallel.mcp.api.key}")
    private String apiKey;

    @Value("${parallel.mcp.api.base-uri}")
    private String baseUri;

    @Value("${parallel.mcp.api.endpoint}")
    private String endpoint;

    @Bean
    public McpSyncClient parallelSearchMcpClient() {
        var transport = HttpClientStreamableHttpTransport
                .builder(baseUri)
                .endpoint(endpoint)
                .openConnectionOnStartup(false)
                .connectTimeout(Duration.ofSeconds(30))
                .asyncHttpRequestCustomizer((builder, method, endpoint, body, context) -> {
                    builder.header("Authorization", "Bearer " + apiKey);
                    return Mono.just(builder);
                })
                .build();

        var clientInfo = new McpSchema.Implementation("parallel-search-client", "1.0.0");

        return McpClient.sync(transport)
                .clientInfo(clientInfo)
                .requestTimeout(Duration.ofSeconds(30))
                .build();
    }
}
