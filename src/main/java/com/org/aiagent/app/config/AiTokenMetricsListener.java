package com.org.aiagent.app.config;

import dev.langchain4j.model.chat.listener.ChatModelErrorContext;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.listener.ChatModelRequestContext;
import dev.langchain4j.model.chat.listener.ChatModelResponseContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * AI 大模型可观测性与极简计费监控器 (Qwen-Plus 版)
 */
@Component
public class AiTokenMetricsListener implements ChatModelListener {

    private static final Logger log = LoggerFactory.getLogger(AiTokenMetricsListener.class);
    private final ThreadLocal<Long> startTime = new ThreadLocal<>();

    // 费率设定：更新为 Qwen-Plus 官方单价
    // 输入单价：0.004元 / 1000 Tokens [参考 Qwen-Plus 最新计费标准]
    private static final BigDecimal INPUT_PRICE_PER_THOUSAND = new BigDecimal("0.004");
    // 输出单价：0.012元 / 1000 Tokens [参考 Qwen-Plus 最新计费标准]
    private static final BigDecimal OUTPUT_PRICE_PER_THOUSAND = new BigDecimal("0.012");

    @Override
    public void onRequest(ChatModelRequestContext requestContext) {
        startTime.set(System.currentTimeMillis());
        log.debug("[LLM] Request started.");
    }

    @Override
    public void onResponse(ChatModelResponseContext responseContext) {
        long costTime = System.currentTimeMillis() - startTime.get();
        startTime.remove();

        if (responseContext.response() != null && responseContext.response().tokenUsage() != null) {
            int input = responseContext.response().tokenUsage().inputTokenCount();
            int output = responseContext.response().tokenUsage().outputTokenCount();
            int total = responseContext.response().tokenUsage().totalTokenCount();

            BigDecimal cost = calculateCost(input, output);

            // 极简冷淡风日志：耗时 | Token明细 | 金额
            log.info("[LLM] {}ms | tokens: {} in, {} out, {} total | cost: ￥{}",
                    costTime, input, output, total, cost);
        }
    }

    @Override
    public void onError(ChatModelErrorContext errorContext) {
        startTime.remove();
        log.error("[LLM] Request failed: ", errorContext.error());
    }

    private BigDecimal calculateCost(int inputTokens, int outputTokens) {
        BigDecimal inputCost = new BigDecimal(inputTokens)
                .divide(new BigDecimal("1000"), 4, RoundingMode.HALF_UP)
                .multiply(INPUT_PRICE_PER_THOUSAND);

        BigDecimal outputCost = new BigDecimal(outputTokens)
                .divide(new BigDecimal("1000"), 4, RoundingMode.HALF_UP)
                .multiply(OUTPUT_PRICE_PER_THOUSAND);

        // 最终保留 6 位小数，确保微小开销也能精确体现
        return inputCost.add(outputCost).setScale(6, RoundingMode.HALF_UP);
    }
}