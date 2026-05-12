package com.org.aiagent.service;


/**
 * 智能旅游助手业务接口
 */
public interface TravelAssistantService {
    /**
     * 与 AI 旅游助手对话
     *
     * @param message   用户的提问
     * @param sessionId 会话ID（用于关联上下文）
     * @return AI 的回复内容
     */
    String doChat(String message, String sessionId);


    /**
     * 创建新的对话狂
     * @return
     */
    String createNewSession();
}
