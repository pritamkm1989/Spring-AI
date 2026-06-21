package com.pkm.SpringAI.controller;

import com.pkm.SpringAI.payload.RagResponse;
import com.pkm.SpringAI.service.RagQueryService;
import com.pkm.SpringAI.service.impl.DocumentIngestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/rag")
@RequiredArgsConstructor
public class RagController {

    private final DocumentIngestionService ingestionService;
    private final RagQueryService queryService;

    @PostMapping("/ingest")
    public ResponseEntity<String> ingest(@RequestParam("file") MultipartFile file)
            throws IOException {
        ingestionService.ingest(file);
        return ResponseEntity.ok("Document ingested successfully");
    }

    @GetMapping("/query")
    public ResponseEntity<RagResponse> query(@RequestParam String question, @RequestParam(required = false) String conversationId) {

        String convId = (conversationId != null) ? conversationId
                : UUID.randomUUID().toString();
        RagResponse answer = queryService.query(question,convId);

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Conversation-Id", convId);
        ResponseEntity<RagResponse> response = new ResponseEntity<>(answer,headers, HttpStatus.OK);
        return response;
    }

    @GetMapping("/check")
    public void check(@RequestParam String question) {
        ingestionService.check(question);

    }
}