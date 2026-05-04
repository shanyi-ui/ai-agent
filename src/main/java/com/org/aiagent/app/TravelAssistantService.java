package com.org.aiagent.app;

import cn.hutool.core.date.DateUtil;
import com.org.aiagent.app.tools.TravelTools;
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

    interface TravelAgent {
        @SystemMessage({
                "你是一位专业的‘智能旅游推荐大师’。现在的真实时间是：{{currentDate}}。",
                "【核心原则】",
                "1. 优先级最高：当前的真实时间（{{currentDate}}）是唯一准确的时间基准。如果知识库（PDF）里的建议与当前月份不符，请【绝对不要】采纳知识库里的旧建议。",
                "2. 知识库使用规范：仅参考知识库中的景点描述、特色路线和避坑指南。如果知识库里提到‘现在是10月’，请忽略它，因为现在是5月。",
                "3. 工具使用规范：只要涉及‘明天’、‘周末’或‘具体天气’，必须调用 getWeather 工具，严禁根据知识库的旧天气信息进行猜测。"
        })
        TokenStream chat(
                @MemoryId String sessionId,
                @dev.langchain4j.service.V("currentDate") String currentDate,
                @UserMessage String userMessage
        );
    }

    private final TravelAgent travelAgent;

    public TravelAssistantService(StreamingChatLanguageModel chatModel,
                                  EmbeddingStore<TextSegment> embeddingStore,
                                  EmbeddingModel embeddingModel,
                                  TravelTools travelTools) {

        EmbeddingStoreContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(3)
                .minScore(0.7)
                .build();

        this.travelAgent = AiServices.builder(TravelAgent.class)
                .streamingChatLanguageModel(chatModel)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(10))
                .contentRetriever(contentRetriever)
                .tools(travelTools) // 注入工具类
                .build();
    }

    public TokenStream doChat(String message, String sessionId) {
        String today = DateUtil.format(DateUtil.date(), "yyyy年MM月dd日") + " " + DateUtil.dayOfWeekEnum(DateUtil.date()).toChinese();
        return travelAgent.chat(sessionId, today, message);
    }
}