package com.pkm.SpringAI.service.impl;

import com.pkm.SpringAI.agent.AgentToolExecutor;
import com.pkm.SpringAI.service.AgenticRagService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AgenticRagServiceImpl implements AgenticRagService {

    private final AgentToolExecutor agentToolExecutor;

    public AgenticRagServiceImpl(AgentToolExecutor agentToolExecutor) {
        this.agentToolExecutor = agentToolExecutor;
    }

    @Override
    public String askAgent(String question) {
        return agentToolExecutor.execute(question);
    }
}
