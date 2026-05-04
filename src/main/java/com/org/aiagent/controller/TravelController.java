package com.org.aiagent.controller;



import com.org.aiagent.rag.TravelKnowledgeService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/travel")
public class TravelController {

    @Autowired
    private TravelKnowledgeService travelKnowledgeService;

    private final ChatClient chatClient;

    public TravelController(ChatClient travelChatClient) {
        this.chatClient = travelChatClient;
    }

    /**
     * 基础对话接口
     * 使用Flux<String> 实现流式输出，前端逐字接收
     * @param message
     * @return
     */
    @GetMapping("/chat")
    public Flux<String> chat(@RequestParam(value = "message", defaultValue = "请给我推荐几个适合这个月去的国内景点") String message) {
        return chatClient.prompt()
                .user(message)
                .stream()
                .content();
    }
    /**
     * 新增：触发 PDF 导入的接口
     */
    @GetMapping("/init-knowledge")
    public String initKnowledge(@RequestParam String path) {
        // 记得把这里的斜杠换成你电脑里 PDF 真实的绝对路径
        return travelKnowledgeService.ingestPdfToMilvus(path);
    }
}
