package com.org.aiagent.controller;

import com.org.aiagent.service.TravelAssistantService;
import com.org.aiagent.service.TravelKnowledgeService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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

    /**
     * 对话接口
     */
    @PostMapping("/chat")
    public String chat(@RequestBody Map<String, String> request) {
        // 从 JSON 中提取参数
        String message = request.get("message");
        String sessionId = request.getOrDefault("sessionId", "user_test_999");

        return travelAssistantService.doChat(message, sessionId);
    }

    @GetMapping("/init-knowledge")
    public String initKnowledge(@RequestParam String path) {
        return travelKnowledgeService.ingestPdfToMilvus(path);
    }
}