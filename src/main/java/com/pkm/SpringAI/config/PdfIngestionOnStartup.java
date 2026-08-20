package com.pkm.SpringAI.config;

import com.pkm.SpringAI.service.impl.DocumentIngestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
@RequiredArgsConstructor
public class PdfIngestionOnStartup implements CommandLineRunner {

   private final DocumentIngestionService documentIngestionService;

    @Override
    public void run(String... args) {
        String filename = "aws-overview.pdf";
        try {
            ClassPathResource resource = new ClassPathResource(filename);
            if (!resource.exists()) {
                log.warn("{} not found on classpath, skipping auto-ingest", filename);
                return;
            }

            log.info("Ingesting {} into vector store...", filename);

            documentIngestionService.ingest(filename,resource);
        } catch (Exception e) {
            log.error("Failed to auto-ingest {}", filename, e);
        }
    }
}
