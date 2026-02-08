package com.hiyuan.demo1.service;

import com.hiyuan.demo1.dto.QaResponse;
import com.hiyuan.demo1.entity.Document;
import com.hiyuan.demo1.entity.DocumentChunk;
import com.hiyuan.demo1.entity.VectorRecord;
import com.hiyuan.demo1.repository.QaHistoryRepository;
import com.hiyuan.demo1.repository.UserRepository;
import com.hiyuan.demo1.repository.VectorRecordRepository;
import com.hiyuan.demo1.util.VectorUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.model.embedding.EmbeddingModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class QaServiceCitationFallbackTest {

    @Mock
    private LlmService llmService;

    @Mock
    private EmbeddingModel embeddingModel;

    @Mock
    private MrlService mrlService;

    @Mock
    private VectorRecordRepository vectorRecordRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private QaHistoryRepository qaHistoryRepository;

    @Mock
    private StreamingChatService streamingChatService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private QaDocumentAccessScopeResolver accessScopeResolver;

    @InjectMocks
    private QaService qaService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(qaService, "minCitations", 2);
    }

    @Test
    void buildCitationsSupplementsToTwoWhenOnlyOnePassesThreshold() {
        VectorRecord high = createRecord("doc-high", "高相关", "高相关内容", new float[]{1f, 0f});
        VectorRecord low = createRecord("doc-low", "低相关", "低相关内容", new float[]{0f, 1f});

        List<QaResponse.CitationInfo> citations = invokeBuildCitations(
                List.of(high, low),
                new float[]{1f, 0f}
        );

        assertEquals(2, citations.size());
    }

    @Test
    void buildCitationsReturnsOnlyTwoFallbackCandidatesWhenAllBelowThreshold() {
        VectorRecord low1 = createRecord("doc-1", "文档1", "内容1", new float[]{0f, 1f});
        VectorRecord low2 = createRecord("doc-2", "文档2", "内容2", new float[]{-1f, 0f});
        VectorRecord low3 = createRecord("doc-3", "文档3", "内容3", new float[]{0f, -1f});

        List<QaResponse.CitationInfo> citations = invokeBuildCitations(
                List.of(low1, low2, low3),
                new float[]{1f, 0f}
        );

        assertEquals(2, citations.size());
    }

    @Test
    void buildCitationsUsesConfiguredMinimumCitationCount() {
        ReflectionTestUtils.setField(qaService, "minCitations", 3);

        VectorRecord low1 = createRecord("doc-a", "文档A", "内容A", new float[]{0f, 1f});
        VectorRecord low2 = createRecord("doc-b", "文档B", "内容B", new float[]{-1f, 0f});
        VectorRecord low3 = createRecord("doc-c", "文档C", "内容C", new float[]{0f, -1f});

        List<QaResponse.CitationInfo> citations = invokeBuildCitations(
                List.of(low1, low2, low3),
                new float[]{1f, 0f}
        );

        assertEquals(3, citations.size());
    }

    @SuppressWarnings("unchecked")
    private List<QaResponse.CitationInfo> invokeBuildCitations(List<VectorRecord> records, float[] queryVector) {
        return (List<QaResponse.CitationInfo>) ReflectionTestUtils.invokeMethod(
                qaService,
                "buildCitations",
                records,
                queryVector
        );
    }

    private VectorRecord createRecord(String docKey, String filename, String content, float[] vector) {
        Document document = new Document();
        document.setId(UUID.nameUUIDFromBytes(docKey.getBytes()));
        document.setFilename(filename);

        DocumentChunk chunk = new DocumentChunk();
        chunk.setContent(content);

        VectorRecord record = new VectorRecord();
        record.setDocument(document);
        record.setChunk(chunk);
        record.setEmbedding(VectorUtils.vectorToString(vector));
        return record;
    }
}
