package com.pkm.SpringAI.service.impl;

import com.pkm.SpringAI.service.EmbeddingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class EmbeddingServiceImpl implements EmbeddingService {

    @Autowired
    private EmbeddingModel embeddingModel;

    @Override
    public float [] embed(String text){
        return embeddingModel.embed(text);
    }

    @Override
    public double findSimilarity(String input1, String input2){
        List<float[]> response = embeddingModel.embed(Arrays.asList(input1, input2));
        log.info("response of input {}",response);
        double similarity = cosineSimilarity(response.get(0),response.get(1) );
        log.info("similarity of input {}",similarity);
        return similarity;
    }

    private double cosineSimilarity(float[] a, float[] b) {
        double dot = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += Math.pow(a[i], 2);
            normB += Math.pow(b[i], 2);
        }

        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
