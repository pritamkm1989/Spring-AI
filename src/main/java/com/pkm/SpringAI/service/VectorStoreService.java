package com.pkm.SpringAI.service;

import com.pkm.SpringAI.payload.SimilarityResponse;

public interface VectorStoreService {
    SimilarityResponse findSimilarity(String query);
}
