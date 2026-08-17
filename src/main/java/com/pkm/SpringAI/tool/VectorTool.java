package com.pkm.SpringAI.tool;

import com.pkm.SpringAI.tool.base.AgenticTool;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@AllArgsConstructor
public class VectorTool implements AgenticTool {


    private final VectorStore vectorStore;

    @Tool(description = "Search the internal knowledge base for relevant context on a topic")
    public String searchKnowledgeBase(
            @ToolParam(description = "Clear, specific search query about the internal documents") String query) {
        log.info("[Search] {}", query);
        List<Document> results = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(query)
                        .topK(5)
                        .build()
        );
        if (results.isEmpty()) {
            return "Vector search returned empty results for: " + query;
        }
        return results.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n---\n"));
    }

    @Tool(description = "Search knowledge base filtered to a specific document category, e.g. 'hr_policy', 'product_docs'")
    public String searchKnowledgeBaseByCategory(
            @ToolParam(description = "Clear, specific search query about the internal documents") String query,
            @ToolParam(description = "Category of documents to filter on, e.g. 'hr_policy', 'product_docs', 'general'") String category) {
        log.info("[Search] {} [Category] {}", query, category);
        List<Document> results = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(query)
                        .topK(5)
                        .filterExpression("category == '" + category + "'")
                        .build()
        );
        if (results.isEmpty()) {
            return "Vector search returned empty results for category '" + category + "': " + query;
        }
        return results.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n---\n"));
    }

}
