package com.pkm.SpringAI.service.impl;

import com.pkm.SpringAI.payload.RagResponse;
import com.pkm.SpringAI.service.RagQueryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RagQueryServiceImpl implements RagQueryService {

    @Autowired
    private  VectorStore vectorStore;

    private final ChatClient chatClient;

    @Autowired
    private ChatMemory chatMemory;


    private static final String PROMPT_TEMPLATE = """
            You are a strict retrieval assistant.
            
            You MUST answer ONLY using the supplied context.
            
            Rules:
            - NEVER use prior knowledge.
            - NEVER use general knowledge.
            - NEVER guess.
            - NEVER infer.
            - If the answer is not explicitly stated in the context, respond exactly:
            
            I don't know.
            
            Context:
            {question_answer_context}
            
            Question:
            {query}
            """;



    public RagQueryServiceImpl(ChatClient.Builder builder, ChatMemory chatMemory, VectorStore vectorStore) {
        this.chatClient = builder
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        QuestionAnswerAdvisor.builder(vectorStore)
                                .searchRequest(SearchRequest.builder()
                                        .topK(5)
                                        .similarityThreshold(0.3)  // lower = more results
                                        .build())
                                .promptTemplate(PromptTemplate.builder().template(PROMPT_TEMPLATE).build())
                                .build()
                )
                .build();
    }


    @Override
    public RagResponse query(String userQuestion,String conversationId) {
        log.info("RagQueryServiceImpl query userQuestion: {}", userQuestion);

        List<Message> history = chatMemory.get(conversationId);
        log.info("RagQueryServiceImpl query history: {}", history);

       String contextualizedQuery = rewriteQueryWithHistory(userQuestion, conversationId);
        log.info("RagQueryServiceImpl query userQuestion: {}", contextualizedQuery);


        var docs = vectorStore.similaritySearch(SearchRequest.builder()
                .query(userQuestion)
                .topK(5)
                .build());

        String answer = chatClient.prompt()
                .user(contextualizedQuery)
                .advisors(advisor -> advisor
                        .param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();

        return new RagResponse(
                answer,
                docs.stream()
                        .map(d -> d.getMetadata().get("source"))
                        .distinct()
                        .toList()
        );
    }

    private String rewriteQueryWithHistory(
            String question,
            String conversationId) {

        List<Message> history =
                chatMemory.get(conversationId);

        String historyText = history.stream()
                .map(Message::getText)
                .collect(Collectors.joining("\n"));

        String prompt = """
                Given the conversation history,
                rewrite the user's question
                into a standalone question.
                
                Conversation:
                %s
                
                Question:
                %s
                
                Return only the rewritten question.
                """
                .formatted(historyText, question);

        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }

}
