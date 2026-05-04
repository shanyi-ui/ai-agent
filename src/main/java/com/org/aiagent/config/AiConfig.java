package com.org.aiagent.config;


import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.time.Duration;



import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
public class AiConfig {

    @Bean
    public RestClientCustomizer restClientCustomizer() {
        return builder -> {
            // 使用 Java 原生的 HttpClient 替代被弃用的 OkHttp
            JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(
                    HttpClient.newBuilder()
                            // 连接超时设为 20 秒
                            .connectTimeout(Duration.ofSeconds(20))
                            .build()
            );

            // 接收大模型响应的读取超时设为 120 秒（关键点！）
            factory.setReadTimeout(Duration.ofSeconds(120));

            builder.requestFactory(factory);
        };
    }
    @Bean
    public ChatClient travelChatClient(ChatClient.Builder builder) {
        // 使用 builder 构建 ChatClient，并注入默认的 System Prompt
        return builder
                .defaultSystem("你是一个专业的中国旅游智能助手。你的任务是根据用户的需求，提供详细、贴心、结构化的旅游规划和建议。如果用户问及非旅游相关的话题，请委婉地引导回旅游主题。")
                .build();
    }
}