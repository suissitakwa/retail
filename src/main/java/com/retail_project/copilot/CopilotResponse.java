package com.retail_project.copilot;

import java.util.List;

public record CopilotResponse(
        String answer,
        List<CopilotAction> actions
) {
}
