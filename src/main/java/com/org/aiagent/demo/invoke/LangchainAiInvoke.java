package com.org.aiagent.demo.invoke;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.output.Response;
import jakarta.annotation.Resource;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * LangChain4j 启动自检测试类
 */
@Component
public class LangchainAiInvoke implements CommandLineRunner {

    // 注入我们在 AiConfig 中配置好的 LangChain4j 流式模型
    @Resource
    private StreamingChatLanguageModel chatModel;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("====== LangChain4j 大模型启动测试开始 ======");
        System.out.print("AI回复: ");

        // 调用大模型
        chatModel.generate("你好，我是善逸", new StreamingResponseHandler<AiMessage>() {

            @Override
            public void onNext(String token) {
                // 每接收到一个字，就直接打印在控制台（不换行）
                System.out.print(token);
            }

            @Override
            public void onComplete(Response<AiMessage> response) {
                // 回答结束时换行
                System.out.println("\n====== LangChain4j 大模型启动测试成功！ ======");
            }

            @Override
            public void onError(Throwable error) {
                System.err.println("\n大模型调用失败，请检查 API-KEY 或网络！");
                error.printStackTrace();
            }
        });
    }
}