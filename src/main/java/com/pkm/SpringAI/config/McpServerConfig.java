package com.pkm.SpringAI.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "mcp.config")
@Data
public class McpServerConfig {

    private List<ServerConfig> servers;

    @Data
    public static class ServerConfig {
        private String name;
        private String baseUri;
        private String endpoint = "/mcp";
        private String sseEndpoint = "/sse";
        private String apiKey;
        private String username;
        private String password;
        private String prefix;
        private String clientVersion = "1.0.0";
        private int connectTimeout = 30;
        private int requestTimeout = 30;
        private String transport = "streamable"; // "sse" or "streamable"
    }
}
