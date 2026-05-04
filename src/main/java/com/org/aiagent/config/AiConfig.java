package com.org.aiagent.config;


import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.time.Duration;



import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;

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
}