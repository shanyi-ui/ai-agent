package com.org.aiagent.controller;

import com.org.aiagent.app.TravelAssistantService;
import com.org.aiagent.rag.TravelKnowledgeService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/travel")
@CrossOrigin // 解决前端跨域
public class TravelController {

    private final TravelAssistantService travelAssistantService;
    private final TravelKnowledgeService travelKnowledgeService;

    // 构造函数注入
    public TravelController(TravelAssistantService travelAssistantService, TravelKnowledgeService travelKnowledgeService) {
        this.travelAssistantService = travelAssistantService;
        this.travelKnowledgeService = travelKnowledgeService;
    }

    /**
     * 对话接口 (自带多轮记忆 + 工具调用 + 知识库)
     */
    @GetMapping("/chat")
    public String chat(@RequestParam(value = "message") String message,
                       @RequestParam(value = "sessionId", defaultValue = "user_test_999") String sessionId) {

        // 直接返回字符串给前端
        return travelAssistantService.doChat(message, sessionId);
    }

    /**
     * 初始化 PDF 知识库接口
     */
    @GetMapping("/init-knowledge")
    public String initKnowledge(@RequestParam String path) {
        return travelKnowledgeService.ingestPdfToMilvus(path);
    }
}