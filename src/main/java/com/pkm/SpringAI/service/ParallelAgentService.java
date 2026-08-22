package com.pkm.SpringAI.service;

import java.util.Map;

public interface ParallelAgentService {
    Map<String, String> invokeParallel(String question);
}
