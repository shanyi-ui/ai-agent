package com.org.aiagent.rag;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import org.springframework.stereotype.Service;

import java.nio.file.Paths;

@Service
public class TravelKnowledgeService {

    private final EmbeddingStore<TextSegment> embeddingStore;
    private final EmbeddingModel embeddingModel;

    public TravelKnowledgeService(EmbeddingStore<TextSegment> embeddingStore, EmbeddingModel embeddingModel) {
        this.embeddingStore = embeddingStore;
        this.embeddingModel = embeddingModel;
    }

    public String ingestPdfToMilvus(String filePath) {
        // 1. 加载本地 PDF 文件
        Document document = FileSystemDocumentLoader.loadDocument(Paths.get(filePath), new ApachePdfBoxDocumentParser());

        // 2. 构建数据摄入流水线：切分 -> 向量化 -> 存入数据库
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(DocumentSplitters.recursive(500, 50)) // 500字一块，保留50字重叠防截断
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();

        // 3. 执行
        ingestor.ingest(document);

        return "LangChain4j 知识库构建成功！PDF 已成功向量化并存入 Milvus。";
    }
}