package com.hiyuan.demo1.service;

import com.hiyuan.demo1.dto.QaRequest;
import com.hiyuan.demo1.dto.QaResponse;
import com.hiyuan.demo1.entity.Document;
import com.hiyuan.demo1.entity.DocumentChunk;
import com.hiyuan.demo1.entity.QaHistory;
import com.hiyuan.demo1.entity.User;
import com.hiyuan.demo1.entity.VectorRecord;
import com.hiyuan.demo1.exception.BusinessException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hiyuan.demo1.repository.QaHistoryRepository;
import com.hiyuan.demo1.repository.UserRepository;
import com.hiyuan.demo1.repository.VectorRecordRepository;
import com.hiyuan.demo1.security.UserPrincipal;
import com.hiyuan.demo1.util.VectorUtils;
import dev.langchain4j.model.embedding.EmbeddingModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 问答服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QaService {

    private final LlmService llmService;
    private final EmbeddingModel embeddingModel;
    private final MrlService mrlService;
    private final VectorRecordRepository vectorRecordRepository;
    private final UserRepository userRepository;
    private final QaHistoryRepository qaHistoryRepository;
    private final StreamingChatService streamingChatService;
    private final ObjectMapper objectMapper;
    private final QaDocumentAccessScopeResolver accessScopeResolver;
    
    // 相似度阈值：低于此值的文档将被过滤
    // 余弦相似度范围 [-1, 1]，通常相关文档 > 0.7，不相关 < 0.5
    private static final double SIMILARITY_THRESHOLD = 0.65;
    // 每个文档最多保留一条引用，避免同一文档重复刷屏
    private static final int MAX_CITATIONS_PER_DOCUMENT = 1;

    @Value("${qa.min-citations:2}")
    private int minCitations;

    /**
     * 处理问答请求
     */
    @Transactional
    public QaResponse ask(QaRequest request) {
        try {
            QaProcessingContext context = prepareContext(request);
            String answer = llmService.generate(context.prompt());
            QaHistory history = saveHistory(context.user(), context.question(), answer, context.startTime());
            log.info("[METRIC][QA] mode=sync, topKHits={}, citationsAfterFilter={}, durationMs={}",
                    context.retrievedCount(),
                    context.citations().size(),
                    System.currentTimeMillis() - context.startTime());
            return QaResponse.builder()
                    .answer(answer)
                    .citations(context.citations())
                    .historyId(history == null ? null : history.getId().toString())
                    .build();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("问答处理失败: {}", e.getMessage(), e);
            throw BusinessException.internalError("问答处理", e);
        }
    }

    public Flux<ServerSentEvent<String>> streamAnswer(QaRequest request) {
        QaProcessingContext context;
        try {
            context = prepareContext(request);
        } catch (BusinessException e) {
            return Flux.just(buildEvent("error", Map.of("message", e.getMessage())));
        } catch (Exception e) {
            log.error("流式问答处理失败: {}", e.getMessage(), e);
            return Flux.just(buildEvent("error", Map.of("message", "问答处理失败: " + e.getMessage())));
        }

        Flux<ServerSentEvent<String>> metaFlux = Flux.just(
                buildEvent("meta", Map.of("citations", context.citations()))
        );

        Flux<ServerSentEvent<String>> deltaFlux = streamingChatService.streamChatCompletion(context.prompt())
                .map(chunk -> {
                    context.appendAnswer(chunk);
                    return buildEvent("delta", Map.of("content", chunk));
                })
                .concatWith(Mono.fromCallable(() -> {
                    QaHistory history = saveHistory(
                            context.user(),
                            context.question(),
                            context.answerBuilder().toString(),
                            context.startTime()
                    );
                    log.info("[METRIC][QA] mode=stream, topKHits={}, citationsAfterFilter={}, durationMs={}",
                            context.retrievedCount(),
                            context.citations().size(),
                            System.currentTimeMillis() - context.startTime());
                    return buildEvent("done", Map.of(
                            "historyId", history == null ? null : history.getId().toString(),
                            "answer", context.answerBuilder().toString(),
                            "citations", context.citations()
                    ));
                }))
                .onErrorResume(ex -> {
                    log.error("流式生成失败: {}", ex.getMessage(), ex);
                    return Flux.just(buildEvent("error", Map.of("message", "流式生成失败: " + ex.getMessage())));
                });

        return metaFlux.concatWith(deltaFlux);
    }

    private QaProcessingContext prepareContext(QaRequest request) {
        log.info("处理问答请求: {}", request.getQuestion());

        long startTime = System.currentTimeMillis();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        QaDocumentAccessScopeResolver.AccessScope accessScope = accessScopeResolver.resolve(authentication);
        UUID userId = accessScope.currentUserId();
        User user = null;
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal userPrincipal) {
            user = userRepository.findById(userId).orElse(null);
            log.info("当前用户: {} (ID: {}), 范围: {}",
                    userPrincipal.getUsername(),
                    userId,
                    accessScope.description());
        } else {
            log.warn(accessScope.description());
        }

        int topK = request.getTopK() == null ? 5 : request.getTopK();
        if (topK < 1) topK = 1;
        if (topK > 20) topK = 20;

        float[] fullQuestionVector = embeddingModel.embed(request.getQuestion()).content().vector();
        float[] truncatedVector = mrlService.truncateVector(fullQuestionVector);
        String queryVector = VectorUtils.vectorToString(truncatedVector);

        log.debug("问题向量维度: {} -> {}", fullQuestionVector.length, truncatedVector.length);

        List<UUID> nearestIds;
        if (accessScope.queryAllDocuments()) {
            nearestIds = vectorRecordRepository.findNearestVectorIds(queryVector, null, topK);
        } else if (accessScope.ownerIds().isEmpty()) {
            nearestIds = List.of();
        } else {
            nearestIds = vectorRecordRepository.findNearestVectorIdsByOwnerIds(queryVector, accessScope.ownerIds(), topK);
        }
        log.info("找到 {} 条相似向量ID", nearestIds.size());

        List<VectorRecord> nearestRecords = nearestIds.isEmpty()
                ? List.of()
                : reorderByIds(nearestIds, vectorRecordRepository.findByIdsWithRelations(nearestIds));

        List<QaResponse.CitationInfo> citations = buildCitations(nearestRecords, truncatedVector);
        log.info("过滤后剩余 {} 条相关引用", citations.size());

        String prompt = buildPromptWithHistory(
                request.getQuestion(),
                citations,
                request.getPreviousQuestion(),
                request.getPreviousAnswer()
        );

        return new QaProcessingContext(user, request.getQuestion(), citations, prompt, startTime, nearestIds.size());
    }

    private List<VectorRecord> reorderByIds(List<UUID> nearestIds, List<VectorRecord> loadedRecords) {
        Map<UUID, VectorRecord> recordMap = loadedRecords.stream()
                .collect(Collectors.toMap(VectorRecord::getId, vr -> vr));
        List<VectorRecord> ordered = new ArrayList<>();
        for (UUID id : nearestIds) {
            VectorRecord record = recordMap.get(id);
            if (record != null) {
                ordered.add(record);
            }
        }
        return ordered;
    }

    private List<QaResponse.CitationInfo> buildCitations(List<VectorRecord> records, float[] truncatedVector) {
        List<CitationCandidate> citations = new ArrayList<>(records.size());
        List<CitationCandidate> candidates = new ArrayList<>(records.size());
        for (VectorRecord vr : records) {
            Document document = vr.getDocument();
            DocumentChunk chunk = vr.getChunk();

            log.debug("VectorRecord ID: {}, Document: {}, Chunk: {}",
                    vr.getId(),
                    document != null ? document.getFilename() : "NULL",
                    chunk != null ? "存在" : "NULL");

            String content = chunk != null ? chunk.getContent() : null;
            if (content != null && content.length() > 2000) {
                content = content.substring(0, 2000);
            }

            Double score = null;
            try {
                float[] docVector = VectorUtils.parseVectorString(vr.getEmbedding());
                score = VectorUtils.cosineSimilarity(truncatedVector, docVector);

                log.info("文档: {}, 相似度分数: {}",
                        document != null ? document.getFilename() : "NULL",
                        score != null ? String.format("%.4f", score) : "NULL");

            } catch (Exception e) {
                log.warn("计算相似度失败: {}", e.getMessage());
            }

            QaResponse.CitationInfo citation = QaResponse.CitationInfo.builder()
                    .documentId(document == null ? null : document.getId().toString())
                    .documentTitle(document == null ? null : document.getFilename())
                    .content(content)
                    .score(score)
                    .build();

            String documentKey;
            if (document != null && document.getId() != null) {
                documentKey = document.getId().toString();
            } else if (document != null && StringUtils.hasText(document.getFilename())) {
                documentKey = document.getFilename();
            } else {
                documentKey = "record:" + vr.getId();
            }

            CitationCandidate candidate = new CitationCandidate(documentKey, citation);
            candidates.add(candidate);
            if (score == null || score >= SIMILARITY_THRESHOLD) {
                citations.add(candidate);
            } else {
                log.info("过滤低相似度文档: {} (score: {}, threshold: {})",
                        document != null ? document.getFilename() : "NULL",
                        String.format("%.4f", score),
                        SIMILARITY_THRESHOLD);
            }
        }

        List<CitationCandidate> deduplicatedCitations = deduplicateCandidatesByDocument(citations);
        if (deduplicatedCitations.size() < citations.size()) {
            log.info("引用按文档去重: {} -> {}", citations.size(), deduplicatedCitations.size());
        }

        int minCitationCount = Math.max(1, minCitations);
        if (deduplicatedCitations.size() < minCitationCount && !candidates.isEmpty()) {
            List<CitationCandidate> deduplicatedCandidates = deduplicateCandidatesByDocument(candidates);
            Map<String, CitationCandidate> supplemented = new LinkedHashMap<>();

            for (CitationCandidate citation : deduplicatedCitations) {
                supplemented.put(citation.documentKey(), citation);
            }
            for (CitationCandidate candidate : deduplicatedCandidates) {
                if (supplemented.size() >= minCitationCount) {
                    break;
                }
                supplemented.putIfAbsent(candidate.documentKey(), candidate);
            }

            if (supplemented.size() > deduplicatedCitations.size()) {
                log.warn("相似度过滤后仅 {} 条，保底补充到 {} 条引用",
                        deduplicatedCitations.size(), supplemented.size());
            }
            return toCitationInfos(new ArrayList<>(supplemented.values()));
        }
        return toCitationInfos(deduplicatedCitations);
    }

    private List<CitationCandidate> deduplicateCandidatesByDocument(List<CitationCandidate> candidates) {
        Map<String, Integer> documentCitationCount = new HashMap<>();
        List<CitationCandidate> deduplicated = new ArrayList<>(candidates.size());

        for (CitationCandidate candidate : candidates) {
            int count = documentCitationCount.getOrDefault(candidate.documentKey(), 0);
            if (count >= MAX_CITATIONS_PER_DOCUMENT) {
                continue;
            }
            documentCitationCount.put(candidate.documentKey(), count + 1);
            deduplicated.add(candidate);
        }

        return deduplicated;
    }

    private List<QaResponse.CitationInfo> toCitationInfos(List<CitationCandidate> candidates) {
        return candidates.stream().map(CitationCandidate::citation).toList();
    }

    private record CitationCandidate(String documentKey, QaResponse.CitationInfo citation) {
    }

    private QaHistory saveHistory(User user, String question, String answer, long startTime) {
        if (!StringUtils.hasText(answer)) {
            return null;
        }
        QaHistory history = new QaHistory();
        history.setUser(user);
        history.setQuestion(question);
        history.setAnswer(answer);
        history.setModelVersion(llmService.getModelInfo());
        history.setResponseTime((int) (System.currentTimeMillis() - startTime));

        try {
            return qaHistoryRepository.save(history);
        } catch (Exception e) {
            log.warn("保存问答历史失败，但不影响问答功能: {}", e.getMessage());
            return null;
        }
    }

    private ServerSentEvent<String> buildEvent(String eventName, Object payload) {
        try {
            return ServerSentEvent.<String>builder()
                    .event(eventName)
                    .data(objectMapper.writeValueAsString(payload))
                    .build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("序列化流式事件失败", e);
        }
    }

    private static class QaProcessingContext {
        private final User user;
        private final String question;
        private final List<QaResponse.CitationInfo> citations;
        private final String prompt;
        private final long startTime;
        private final int retrievedCount;
        private final StringBuilder answerBuilder = new StringBuilder();

        QaProcessingContext(User user,
                            String question,
                            List<QaResponse.CitationInfo> citations,
                            String prompt,
                            long startTime,
                            int retrievedCount) {
            this.user = user;
            this.question = question;
            this.citations = List.copyOf(citations);
            this.prompt = prompt;
            this.startTime = startTime;
            this.retrievedCount = retrievedCount;
        }

        public User user() {
            return user;
        }

        public String question() {
            return question;
        }

        public List<QaResponse.CitationInfo> citations() {
            return citations;
        }

        public String prompt() {
            return prompt;
        }

        public long startTime() {
            return startTime;
        }

        public int retrievedCount() {
            return retrievedCount;
        }

        public void appendAnswer(String chunk) {
            this.answerBuilder.append(chunk);
        }

        public StringBuilder answerBuilder() {
            return answerBuilder;
        }
    }

    /**
     * 构建提示词
     */
    private String buildPrompt(String question, List<QaResponse.CitationInfo> citations) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("你是一位经验丰富的教育专家，擅长讲解教材和学习指南。\n");
        prompt.append("请帮助学生深入理解问题，而不仅仅是给出简短答案。\n\n");

        prompt.append("【回答要求】\n");
        prompt.append("- 目标长度：500-1500字，详细但不啰嗦\n");
        prompt.append("- 使用Markdown格式，关键概念用**粗体**\n");
        prompt.append("- 如果资料不足，说明依据有限\n\n");

        prompt.append("【答案结构（按顺序）】\n");
        prompt.append("1. **直接回答**（50-100字）：开门见山给出核心答案\n");
        prompt.append("2. **详细解释**（200-500字）：分点说明，使用类比帮助理解\n");
        prompt.append("3. **实际应用**（100-200字）：举2个贴近生活或课程的案例\n");
        prompt.append("4. **关键要点总结**（50-100字）：用✓列出3-5条重要结论\n\n");

        if (citations.isEmpty()) {
            prompt.append("【参考资料】\n无可用资料，请基于常识回答，但避免编造。\n\n");
        } else {
            prompt.append("【参考资料】\n");
            for (int i = 0; i < citations.size(); i++) {
                prompt.append(String.format("[参考%d] %s\n", i + 1, citations.get(i).getContent()));
            }
            prompt.append("\n");
        }

        prompt.append("【问题】").append(question).append("\n\n");
        prompt.append("请严格按照上述结构回答：\n");

        return prompt.toString();
    }

    private String buildPromptWithHistory(String question,
                                          List<QaResponse.CitationInfo> citations,
                                          String previousQuestion,
                                          String previousAnswer) {
        StringBuilder prompt = new StringBuilder();

        if (StringUtils.hasText(previousQuestion) || StringUtils.hasText(previousAnswer)) {
            prompt.append("【历史对话】\n");
            if (StringUtils.hasText(previousQuestion)) {
                prompt.append("学生上一问：").append(previousQuestion).append("\n");
            }
            if (StringUtils.hasText(previousAnswer)) {
                prompt.append("你的上一答：").append(previousAnswer).append("\n");
            }
            prompt.append("请参考以上上下文，保持回答连贯。\n\n");
        }

        prompt.append(buildPrompt(question, citations));
        return prompt.toString();
    }
}
