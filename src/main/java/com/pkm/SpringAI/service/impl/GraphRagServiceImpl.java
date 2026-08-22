package com.pkm.SpringAI.service.impl;

import com.pkm.SpringAI.graph.AgentGraphToolExecutor;
import com.pkm.SpringAI.service.GraphRagService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class GraphRagServiceImpl implements GraphRagService {

    private final AgentGraphToolExecutor agentGraphToolExecutor;

    public GraphRagServiceImpl(AgentGraphToolExecutor agentGraphToolExecutor) {
        this.agentGraphToolExecutor = agentGraphToolExecutor;
    }

    @Override
    public String askGraphAgent(String question) {
        return agentGraphToolExecutor.execute(question);
    }
}
