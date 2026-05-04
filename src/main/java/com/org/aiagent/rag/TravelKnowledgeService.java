package com.org.aiagent.rag;

// 注意看这里，这是正确的 Document 包！
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TravelKnowledgeService {

    private final VectorStore vectorStore;

    public TravelKnowledgeService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    /**
     * 将本地 PDF 文件读取、切块并存入 Milvus 向量数据库
     */
    public String ingestPdfToMilvus(String filePath) {
        // 1. 读取 PDF：使用 Spring AI 提供的 PDF 阅读器
        PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(
                "file:" + filePath,
                PdfDocumentReaderConfig.builder().build()
        );
        List<Document> documents = pdfReader.get();

        // 2. 文本切块：因为大模型有上下文长度限制，我们不能把一整本书直接塞进去
        // 我们把它切成一小段一小段的（这里使用默认的分词器）
        TokenTextSplitter textSplitter = new TokenTextSplitter();
        List<Document> splitDocuments = textSplitter.apply(documents);

        // 3. 存入数据库：这简单的一行代码，背后会自动调用 Embedding 模型把文字转成向量，并存入 Milvus
        vectorStore.add(splitDocuments);

        return "知识库构建成功！共存入 " + splitDocuments.size() + " 个文本块到 Milvus 数据库中。";
    }
}