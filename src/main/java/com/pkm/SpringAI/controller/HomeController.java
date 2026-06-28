package com.pkm.SpringAI.controller;

import com.pkm.SpringAI.payload.SimilarityResponse;
import com.pkm.SpringAI.service.ChatService;
import com.pkm.SpringAI.service.EmbeddingService;
import com.pkm.SpringAI.service.VectorStoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class HomeController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private EmbeddingService embeddingService;

    @Autowired
    private VectorStoreService vectorStoreService;

    @GetMapping
    public String sayHello(){
        return "Hello";
    }

    @GetMapping(value ="chat",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat(@RequestParam String message){
        return chatService.chatPrompt(message);
    }

    @GetMapping("embed")
    public float[] embed(@RequestParam String message){
        return embeddingService.embed(message);
    }

    @GetMapping("similarity")
    public double embed(@RequestParam String input1,@RequestParam String input2){
        return embeddingService.findSimilarity(input1,input2) * 100;
    }

    @GetMapping("similaritySearch")
    public SimilarityResponse similaritySearch(@RequestParam String input1){
         return vectorStoreService.findSimilarity(input1);
    }
}
