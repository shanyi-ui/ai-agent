package com.org.aiagent.app;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.data.segment.TextSegment;
import org.springframework.stereotype.Service;

@Service
public class TravelAssistantService {

    // 1. 定义 LangChain4j 的声明式 AI 接口 (黑魔法核心：只需要定义接口和注解)
    interface TravelAgent {
        @SystemMessage({
                "你是一位深耕文旅行业的‘智能旅游推荐大师’。",
                "你的职责是为用户提供精准、个性化的旅游路线规划、景点推荐和避坑指南。",
                "请务必优先参考检索到的独家知识库攻略来回答用户的问题。",
                "语气要热情、专业，像一位资深的导游朋友。"
        })
            // @MemoryId 用于区分不同用户的聊天窗口，实现千人千面的记忆
        TokenStream chat(@MemoryId String sessionId, @UserMessage String userMessage);
    }

    private final TravelAgent travelAgent;

    // 2. 在构造函数中，将大脑、记忆和 Milvus 数据库组装起来！
    public TravelAssistantService(StreamingChatLanguageModel chatModel,
                                  EmbeddingStore<TextSegment> embeddingStore,
                                  EmbeddingModel embeddingModel) {

        // A. 配置 RAG 检索器：让大模型在每次回答前，先去 Milvus 里面查相关的旅游攻略
        EmbeddingStoreContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(3) // 每次最多抓取 3 段最相关的攻略文本
                .minScore(0.7) // 相似度低于 0.7 的不要，防止胡说八道
                .build();

        // B. 组装终极 Agent：注入流式大模型、动态记忆体、知识库检索器
        this.travelAgent = AiServices.builder(TravelAgent.class)
                .streamingChatLanguageModel(chatModel)
                // 为每个不同的 sessionId 分配独立的记忆盒子，最多记住最近 10 条对话
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(10))
                .contentRetriever(contentRetriever)
                .build();
    }

    // 3. 对外暴露聊天方法
    public TokenStream doChat(String message, String sessionId) {
        return travelAgent.chat(sessionId, message);
    }
}