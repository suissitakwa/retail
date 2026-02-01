package com.retail_project.copilot;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/copilot")
@RequiredArgsConstructor
public class CopilotController {
   private final CopilotService copilotService;


   @PostMapping("/chat")
   public CopilotResponse chat(@RequestBody CopilotRequest req, Authentication auth){
       return copilotService.chat(req,auth);
   }


}
