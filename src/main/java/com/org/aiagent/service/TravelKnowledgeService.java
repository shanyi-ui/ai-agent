package com.org.aiagent.service;


/**
 * RAG 旅游知识库管理接口
 */
public interface TravelKnowledgeService {
    /**
     * 将本地 PDF 文件加载、切分并存入 Milvus 向量数据库
     *
     * @param filePath PDF 文件的绝对路径
     * @return 处理结果提示
     */
    String ingestPdfToMilvus(String filePath);
}
