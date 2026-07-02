package com.retail_project.debugcopilot;

public record DebugCopilotRequest(String traceId, String serviceName, Integer lookbackMinutes) {
}
