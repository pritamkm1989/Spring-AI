package com.pkm.SpringAI.service.impl;

import com.pkm.SpringAI.payload.SimilarityResponse;
import com.pkm.SpringAI.service.VectorStoreService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class VectorStoreServiceImpl implements VectorStoreService {

    @Autowired
    private VectorStore vectorStore;



    @Override
    public SimilarityResponse findSimilarity(String query) {
        List<Document> result = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(query)
                        .topK(1)
                        .build()
        );
        log.info("result: {}", result);
        Document document = result.getFirst();
        log.info("result: {} and score {} Metadata {}" , document.getText(),document.getScore(),document.getMetadata() );
        return SimilarityResponse.builder().similarity(document.getText()).score( (document.getScore() * 100) + "%").build();
    }

}
