package com.pkm.SpringAI.tool.base;

public interface AgenticTool {

    default int getMaxCallsPerQuestion() {
        return 5;
    }
}
