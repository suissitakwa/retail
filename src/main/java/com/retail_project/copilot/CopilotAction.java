package com.retail_project.copilot;
import java.util.Map;
public record CopilotAction(
        String name,
        Map<String, Object> input,
        String outputSummary
) {
}
