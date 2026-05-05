package com.org.aiagent.rag;

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

@Service
public class TravelKnowledgeService {

    private static final int CHUNK_SIZE = 500;
    private static final int CHUNK_OVERLAP = 50;

    private final EmbeddingStoreIngestor ingestor;

    public TravelKnowledgeService(EmbeddingStore<TextSegment> embeddingStore, EmbeddingModel embeddingModel) {
        // PDF 文本清洗器：将连续的空白符和换行替换为单空格，防止切分后出现碎片化无意义文本
        TextSegmentTransformer textCleaner = segment -> TextSegment.from(
                segment.text().replaceAll("\\s+", " ").trim(),
                segment.metadata()
        );

        // 全局复用单个 Ingestor 实例
        this.ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(DocumentSplitters.recursive(CHUNK_SIZE, CHUNK_OVERLAP))
                .textSegmentTransformer(textCleaner)
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();
    }

    public String ingestPdfToMilvus(String filePath) {
        Document document = FileSystemDocumentLoader.loadDocument(
                Paths.get(filePath),
                new ApachePdfBoxDocumentParser()
        );

        this.ingestor.ingest(document);

        return "知识库构建成功！PDF 已向量化并存入 Milvus。";
    }
}