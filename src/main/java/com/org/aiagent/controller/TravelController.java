package com.org.aiagent.controller;

import com.org.aiagent.service.TravelAssistantService;
import com.org.aiagent.service.TravelKnowledgeService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/travel")
@CrossOrigin
public class TravelController {

    // 依赖注入的是 Service 接口，符合依赖倒置原则
    private final TravelAssistantService travelAssistantService;
    private final TravelKnowledgeService travelKnowledgeService;

    public TravelController(TravelAssistantService travelAssistantService, TravelKnowledgeService travelKnowledgeService) {
        this.travelAssistantService = travelAssistantService;
        this.travelKnowledgeService = travelKnowledgeService;
    }

    @GetMapping("/chat")
    public String chat(@RequestParam(value = "message") String message,
                       @RequestParam(value = "sessionId", defaultValue = "user_test_999") String sessionId) {
        return travelAssistantService.doChat(message, sessionId);
    }

    @GetMapping("/init-knowledge")
    public String initKnowledge(@RequestParam String path) {
        return travelKnowledgeService.ingestPdfToMilvus(path);
    }
}