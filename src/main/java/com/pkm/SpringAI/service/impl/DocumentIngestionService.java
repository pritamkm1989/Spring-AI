package com.pkm.SpringAI.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentIngestionService {
    private final VectorStore vectorStore;
    private final TokenTextSplitter textSplitter;

    public void ingest(MultipartFile file) throws IOException {
        // 1. Load document (Tika handles PDF, DOCX, HTML, etc.)
        Resource resource = new InputStreamResource(file.getInputStream());
        TikaDocumentReader reader = new TikaDocumentReader(resource);
        List<Document> rawDocs = reader.get();

        // 2. Split into overlapping chunks
        List<Document> chunks = textSplitter.apply(rawDocs);
        AtomicInteger chunkCounter = new AtomicInteger(1);
        // 3. Add source metadata
        chunks.forEach(doc -> {
            doc.getMetadata().put("source", file.getOriginalFilename());
            doc.getMetadata().put("chunkId", chunkCounter.getAndIncrement());
            doc.getMetadata().put("ingestedAt", LocalDateTime.now().toString());
        });

        // 4. Embed + store (Spring AI handles the embedding call)
        vectorStore.add(chunks);
    }

    public void check(String question)  {
        List<Document> docs = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(question)
                        .topK(3)
                        .similarityThreshold(0.3)  // very low to catch anything
                        .build()
        );

        if (docs.isEmpty()) {
            log.info("Vector store is EMPTY — ingest documents first!");
        }


       log.info(String.valueOf(docs.stream()
                .map(d -> d.getText().substring(0, Math.min(200, d.getText().length())))
                .toList()));

    }
}
