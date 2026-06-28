package com.pkm.SpringAI.service;

import reactor.core.publisher.Flux;

public interface ChatService {

    Flux<String> chatPrompt(String  prompt);
}
