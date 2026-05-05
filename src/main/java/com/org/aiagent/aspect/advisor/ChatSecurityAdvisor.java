package com.org.aiagent.aspect.advisor;


import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * AI 提问安全前置拦截器 (Advisor)
 */
//存放AOP安全拦截与审计
@Aspect
@Component
public class ChatSecurityAdvisor {

    private static final Logger log = LoggerFactory.getLogger(ChatSecurityAdvisor.class);

    // 模拟敏感词库（实际企业开发中，这通常是从数据库或 Redis 中读取的）
    private static final List<String> SENSITIVE_WORDS = Arrays.asList("破解", "违法", "涉政", "机密","黄色","色情");

    @Around("execution(* com.org.aiagent.service.TravelAssistantService.doChat(..))")
    public Object checkSecurity(ProceedingJoinPoint pjp) throws Throwable {
        Object[] args = pjp.getArgs();

        // 遍历所有参数，只要是字符串，就全部进行敏感词扫描
        for (Object arg : args) {
            if (arg instanceof String) {
                String strArg = (String) arg;

                // 敏感词校验
                for (String word : SENSITIVE_WORDS) {
                    if (strArg.contains(word)) {
                        log.warn("[Security] 触发安全守卫：检测到非法词汇 [{}]，已拦截请求！", word);
                        return "对不起，您的提问包含不合规内容，系统已拦截该请求。";
                    }
                }
            }
        }

        // 校验通过，放行请求
        return pjp.proceed();
    }
}