package com.org.aiagent.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.dashscope.QwenChatModel;
import dev.langchain4j.model.dashscope.QwenEmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.milvus.MilvusEmbeddingStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections; // 引入用于创建 List

@Configuration
public class AiConfig {

    @Value("${langchain4j.dashscope.api-key}")
    private String apiKey;

    // 1. 注册对话大模型，并挂载监听器
    @Bean
    public ChatLanguageModel chatModel(AiTokenMetricsListener tokenListener) {
        //在参数中注入你写好的 tokenListener
        return QwenChatModel.builder()
                .apiKey(apiKey)
                .modelName("qwen-plus")
                //将监听器注册到模型中
                .listeners(Collections.singletonList(tokenListener))
                .build();
    }

    // 2. 注册向量化模型
    @Bean
    public EmbeddingModel embeddingModel() {
        return QwenEmbeddingModel.builder()
                .apiKey(apiKey)
                .modelName("text-embedding-v2")
                .build();
    }

    // 3. 注册 Milvus 向量数据库连接
    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        return MilvusEmbeddingStore.builder()
                .host("localhost")
                .port(19530)
                .collectionName("travel_knowledge")
                .dimension(1536)
                .build();
    }
}