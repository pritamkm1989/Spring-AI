package com.pkm.SpringAI.service;

public interface EmbeddingService {
    float [] embed(String text);

    double findSimilarity(String input1, String input2);
}
