package com.org.aiagent.app.memory;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 企业级 Redis 记忆存储器
 */
@Component
public class RedisChatMemoryStore implements ChatMemoryStore {

    // 1. 使用标准日志框架
    private static final Logger log = LoggerFactory.getLogger(RedisChatMemoryStore.class);

    // 2. 规范化 Redis Key 前缀，防止与其他业务数据冲突
    private static final String REDIS_KEY_PREFIX = "ai:travel:memory:";

    // 3. 将魔法值提取为常量 (记忆保留 30 天)
    private static final long EXPIRE_DAYS = 30;

    private final StringRedisTemplate redisTemplate;

    public RedisChatMemoryStore(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        String key = REDIS_KEY_PREFIX + memoryId;
        String json = redisTemplate.opsForValue().get(key);

        // 使用 Spring 自带工具类进行更安全的字符串判空
        if (!StringUtils.hasText(json)) {
            log.debug("未找到历史记忆，开启新对话。SessionId: {}", memoryId);
            return new ArrayList<>();
        }

        log.debug("成功加载历史记忆。SessionId: {}", memoryId);
        return ChatMessageDeserializer.messagesFromJson(json);
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        String key = REDIS_KEY_PREFIX + memoryId;
        String json = ChatMessageSerializer.messagesToJson(messages);

        redisTemplate.opsForValue().set(key, json, EXPIRE_DAYS, TimeUnit.DAYS);
        log.debug("历史记忆更新成功。SessionId: {}", memoryId);
    }

    @Override
    public void deleteMessages(Object memoryId) {
        String key = REDIS_KEY_PREFIX + memoryId;
        redisTemplate.delete(key);
        log.info("用户记忆已清空。SessionId: {}", memoryId);
    }
}