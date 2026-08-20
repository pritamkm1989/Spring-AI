package com.pkm.SpringAI.agent;

import com.pkm.SpringAI.tool.base.ToolRateLimiter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@AllArgsConstructor
public class AgentGraphToolExecutor {

    private static final int MAX_STEPS = 10;

    private final ToolClassifierAgent classifier;
    private final ToolExecutor executor;
    private final AnswerGeneratorAgent answerGenerator;
    private final ToolRateLimiter toolRateLimiter;



    public String execute(String question) {
        log.info("[AgentGraph] === START === question: {}", question);
        toolRateLimiter.reset();
        List<String> observations = new ArrayList<>();
        List<String> toolsCalled = new ArrayList<>();

        for (int step = 0; step < MAX_STEPS; step++) {
            log.info("[AgentGraph] Step {}: classifying...", step + 1);

            String observationsText = observations.isEmpty()
                    ? "No observations yet."
                    : String.join("\n---\n", observations);

            // Step 1: Classify next action (local qwen2.5:7b)
            ToolClassification classification = classifier.classify(question, observationsText);

            // If FINAL_ANSWER or unknown action, stop
            if (classification.isFinalAnswer() || classification.getAction() == null) {
                log.info("[AgentGraph] Step {}: FINAL_ANSWER ({} observations collected)", step + 1, observations.size());
                break;
            }

            // Step 2: Execute the tool deterministically
            log.info("[AgentGraph] Step {}: executing {}...", step + 1, classification.getAction());
            String result = executor.execute(classification);

            toolsCalled.add(classification.getAction());
            observations.add("[" + classification.getAction() + "] " + result);

            log.info("[AgentGraph] Step {}: {} returned {} chars", step + 1, classification.getAction(), result.length());
        }

        // Step 3: Generate final answer (cloud gemma4)
        log.info("[AgentGraph] Generating answer with {} observations...", observations.size());
        String answer = answerGenerator.generate(question, observations, toolsCalled);

        log.info("[AgentGraph] === END ===");
        return answer;
    }
}
