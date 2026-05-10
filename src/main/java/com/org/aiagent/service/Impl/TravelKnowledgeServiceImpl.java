package com.org.aiagent.service.Impl;



import com.org.aiagent.service.TravelKnowledgeService;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.data.segment.TextSegmentTransformer;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import org.springframework.stereotype.Service;

import java.nio.file.Paths;

/**
 * RAG 知识库管理实现类
 */
@Service
public class TravelKnowledgeServiceImpl implements TravelKnowledgeService {

    private static final int CHUNK_SIZE = 500;
    private static final int CHUNK_OVERLAP = 50;

    private final EmbeddingStoreIngestor ingestor;

    public TravelKnowledgeServiceImpl(EmbeddingStore<TextSegment> embeddingStore, EmbeddingModel embeddingModel) {
        // PDF 文本清洗器
        TextSegmentTransformer textCleaner = segment -> TextSegment.from(
                segment.text().replaceAll("\\s+", " ").trim(),
                segment.metadata()
        );

        this.ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(DocumentSplitters.recursive(CHUNK_SIZE, CHUNK_OVERLAP))
                .textSegmentTransformer(textCleaner)
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();
    }

    @Override
    public String ingestPdfToMilvus(String filePath) {
        Document document = FileSystemDocumentLoader.loadDocument(
                Paths.get(filePath),
                new ApachePdfBoxDocumentParser()
        );

        this.ingestor.ingest(document);

        return "知识库构建成功！PDF 已向量化并存入 Milvus。";
    }
}
