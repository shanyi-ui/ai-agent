package com.org.aiagent.demo.invoke;

import dev.langchain4j.model.chat.ChatLanguageModel; //
import jakarta.annotation.Resource; //
import org.springframework.boot.CommandLineRunner; //[cite: 3]
import org.springframework.stereotype.Component; //[cite: 3]

/**
 * LangChain4j 启动自检测试类
 * 已修改为非流式调用，以支持 Tools 功能[cite: 3]
 */
@Component
public class LangchainAiInvoke implements CommandLineRunner {

    // 注入我们在 AiConfig 中配置好的非流式模型[cite: 3]
    @Resource
    private ChatLanguageModel chatModel;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("====== LangChain4j 大模型启动测试开始 ======");

        try {
            //不再使用 StreamingResponseHandler，直接一行代码获取结果
            String response = chatModel.generate("你好，我是善逸");

            System.out.println("AI回复: " + response);
            System.out.println("====== LangChain4j 大模型启动测试成功！ ======");
        } catch (Exception e) {
            System.err.println("\n大模型调用失败，请检查 API-KEY 或网络！");
            e.printStackTrace();
        }
    }
}