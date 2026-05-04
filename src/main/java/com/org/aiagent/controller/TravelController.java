package com.org.aiagent.controller;

import com.org.aiagent.rag.TravelKnowledgeService;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.output.Response;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@RestController
@RequestMapping("/api/travel")
public class TravelController {

    private final StreamingChatLanguageModel chatModel;
    private final TravelKnowledgeService travelKnowledgeService;

    public TravelController(StreamingChatLanguageModel chatModel, TravelKnowledgeService travelKnowledgeService) {
        this.chatModel = chatModel;
        this.travelKnowledgeService = travelKnowledgeService;
    }

    /**
     * 流式对话接口 (使用 Spring MVC 原生的 SseEmitter)
     */
    @GetMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(@RequestParam(value = "message", defaultValue = "请给我推荐几个适合这个月去的国内景点") String message) {
        // 创建一个 SseEmitter，0L 表示永不超时
        SseEmitter emitter = new SseEmitter(0L);

        // LangChain4j 正确的流式调用方法是 generate
        chatModel.generate(message, new StreamingResponseHandler<AiMessage>() {

            @Override
            public void onNext(String token) {
                try {
                    // 每当大模型吐出一个字 (token)，就发送给前端
                    emitter.send(token);
                } catch (IOException e) {
                    emitter.completeWithError(e);
                }
            }

            @Override
            public void onComplete(Response<AiMessage> response) {
                // 回答彻底结束，关闭流
                emitter.complete();
            }

            @Override
            public void onError(Throwable error) {
                // 发生异常时报错并关闭
                emitter.completeWithError(error);
            }
        });

        return emitter;
    }

    /**
     * 初始化 PDF 知识库接口
     */
    @GetMapping("/init-knowledge")
    public String initKnowledge(@RequestParam String path) {
        return travelKnowledgeService.ingestPdfToMilvus(path);
    }
}