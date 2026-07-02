package com.retail_project.debugcopilot;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/debug-copilot")
@RequiredArgsConstructor
public class DebugCopilotController {

    private final DebugCopilotService debugCopilotService;

    @PostMapping("/analyze")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public DebugCopilotResponse analyze(@RequestBody DebugCopilotRequest req) {
        return debugCopilotService.analyze(req);
    }
}
