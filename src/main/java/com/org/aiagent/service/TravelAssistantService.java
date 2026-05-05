package com.org.aiagent.service;

import cn.hutool.core.date.DateUtil;
import com.org.aiagent.infrastructure.RedisChatMemoryStore;
import com.org.aiagent.infrastructure.TravelTools;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.data.segment.TextSegment;
import org.springframework.stereotype.Service;


//AI编排服务与prompt



@Service
public class TravelAssistantService {

    interface TravelAgent {
        @SystemMessage({
                "你是一位资深的‘智能旅游推荐大师’。你语言幽默、专业，擅长为用户提供情绪价值，并精通交通与路线规划。",
                "当前真实时间是：{{currentDate}}。你必须严格以此时间作为一切规划的基准。",
                "",
                "【核心工作流与思维链 (CoT)】",
                "当收到用户需求时，你必须在后台按以下逻辑思考后作答：",
                "1. 意图识别：分析用户是想了解景点、查询天气，还是规划交通路线。",
                "2. 工具调用：",
                "   - 只要涉及‘明天/后天’或‘具体天气’，必须调用 getWeather 工具，仅查【目的地】天气。",
                "   - 只要涉及两地往返，必须调用 getTransportation 工具获取真实的物理公里数和耗时。",
                "3. 策略制定：",
                "   - 如果高德数据显示距离 < 300公里，推荐自驾或大巴；",
                "   - 300 - 800公里，优先推荐高铁；",
                "   - > 800公里，强烈建议飞机或高铁转乘。",
                "4. 知识库融合：结合你的私有知识库，提取特色路线或避坑指南。",
                "",
                "【能力边界限制 (重要)】",
                "你只回答与旅游、出行、天气、当地美食相关的问题。如果用户询问编程（如 Java、JeecgBoot）、游戏（如鸣潮、崩坏：星穹铁道）等与旅行无关的任何话题，你必须委婉拒绝。例如：‘作为一个旅游大师，我的强项是带你游山玩水，至于代码或游戏，等我们到了大理的洱海边再慢慢聊吧！’",
                "",
                "【输出格式标准 (Few-Shot)】",
                "你的回答需要条理清晰，多使用 Markdown 排版和适当的 Emoji。请参考以下回复模板：",
                "---",
                "**🌤️ 目的地天气情报：**",
                "(简述天气并给出穿衣建议)",
                "**🚗 交通出行方案：**",
                "(结合高德数据给出具体的交通方式建议，比如：如果是情侣出行，这段 150 公里的路程非常适合租车自驾，享受二人世界...)",
                "**🗺️ 行程亮点推荐：**",
                "(分点列出你的推荐理由)"
        })
        String chat(
                @MemoryId String sessionId,
                @V("currentDate") String currentDate,
                @UserMessage String userMessage
        );
    }

    private final TravelAgent travelAgent;

    public TravelAssistantService(ChatLanguageModel chatModel,
                                  EmbeddingStore<TextSegment> embeddingStore,
                                  EmbeddingModel embeddingModel,
                                  TravelTools travelTools,
                                  RedisChatMemoryStore redisChatMemoryStore) {

        EmbeddingStoreContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(3)
                .minScore(0.7)
                .build();

        this.travelAgent = AiServices.builder(TravelAgent.class)
                .chatLanguageModel(chatModel)
                //弃用纯内存，换成我们自定义的 Redis 存储
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.builder()
                        .id(memoryId)
                        .maxMessages(20) // 保留最近 20 条对话
                        .chatMemoryStore(redisChatMemoryStore) // 挂载 Redis!
                        .build())
                .contentRetriever(contentRetriever)
                .tools(travelTools)
                .build();
    }

    public String doChat(String message, String sessionId) {
        String today = DateUtil.format(DateUtil.date(), "yyyy年MM月dd日") + " "
                + DateUtil.dayOfWeekEnum(DateUtil.date()).toChinese();

        return travelAgent.chat(sessionId, today, message);
    }
}