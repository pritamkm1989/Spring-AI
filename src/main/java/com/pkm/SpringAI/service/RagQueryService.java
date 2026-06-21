package com.pkm.SpringAI.service;

import com.pkm.SpringAI.payload.RagResponse;

public interface RagQueryService {
    RagResponse query(String userQuestion,String conversationId);
}
