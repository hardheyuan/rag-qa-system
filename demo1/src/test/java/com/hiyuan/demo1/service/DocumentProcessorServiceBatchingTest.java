package com.hiyuan.demo1.service;

import com.hiyuan.demo1.entity.Document;
import com.hiyuan.demo1.entity.DocumentChunk;
import com.hiyuan.demo1.repository.DocumentChunkRepository;
import com.hiyuan.demo1.repository.DocumentRepository;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentProcessorServiceBatchingTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private DocumentChunkRepository chunkRepository;

    @Mock
    private VectorStorageService vectorStorageService;

    @Mock
    private EmbeddingModel embeddingModel;

    @Mock
    private MrlService mrlService;

    @Mock
    private AliyunOcrService aliyunOcrService;

    @InjectMocks
    private DocumentProcessorService documentProcessorService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(documentProcessorService, "embeddingBatchSize", 16);
    }

    @Test
    void saveChunksAndVectorsUsesBatchEmbedding() {
        Document document = new Document();
        document.setFilename("test.pdf");
        ReflectionTestUtils.setField(document, "id", UUID.randomUUID());

        when(chunkRepository.save(any(DocumentChunk.class))).thenAnswer(invocation -> {
            DocumentChunk chunk = invocation.getArgument(0);
            ReflectionTestUtils.setField(chunk, "id", UUID.randomUUID());
            return chunk;
        });

        when(embeddingModel.embedAll(any()))
                .thenReturn(Response.from(List.of(
                        new Embedding(new float[]{1f, 2f}),
                        new Embedding(new float[]{3f, 4f})
                )));
        when(mrlService.truncateVector(any(float[].class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(() -> documentProcessorService.saveChunksAndVectors(
                document,
                List.of("第一段内容", "第二段内容")
        ));

        verify(embeddingModel, times(1)).embedAll(any());
        verify(vectorStorageService, times(2)).insertVectorRecord(
                any(UUID.class), any(UUID.class), any(UUID.class), anyString(), any(Integer.class), anyString());
    }

    @Test
    void saveChunksAndVectorsFallsBackToSingleEmbeddingWhenBatchFails() {
        Document document = new Document();
        document.setFilename("test.pdf");
        ReflectionTestUtils.setField(document, "id", UUID.randomUUID());

        when(chunkRepository.save(any(DocumentChunk.class))).thenAnswer(invocation -> {
            DocumentChunk chunk = invocation.getArgument(0);
            ReflectionTestUtils.setField(chunk, "id", UUID.randomUUID());
            return chunk;
        });

        when(embeddingModel.embedAll(any())).thenThrow(new RuntimeException("batch failed"));
        when(embeddingModel.embed(anyString()))
                .thenReturn(Response.from(new Embedding(new float[]{5f, 6f})));
        when(mrlService.truncateVector(any(float[].class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(() -> documentProcessorService.saveChunksAndVectors(
                document,
                List.of("A", "B")
        ));

        verify(embeddingModel, times(1)).embedAll(any());
        verify(embeddingModel, times(2)).embed(anyString());
        verify(vectorStorageService, times(2)).insertVectorRecord(
                any(UUID.class), any(UUID.class), any(UUID.class), anyString(), any(Integer.class), anyString());
    }
}
