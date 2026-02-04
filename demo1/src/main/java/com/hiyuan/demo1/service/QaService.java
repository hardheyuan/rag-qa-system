package com.hiyuan.demo1.service;

import com.hiyuan.demo1.dto.QaRequest;
import com.hiyuan.demo1.dto.QaResponse;
import com.hiyuan.demo1.entity.Document;
import com.hiyuan.demo1.entity.DocumentChunk;
import com.hiyuan.demo1.entity.QaHistory;
import com.hiyuan.demo1.entity.User;
import com.hiyuan.demo1.entity.VectorRecord;
import com.hiyuan.demo1.exception.BusinessException;
import com.hiyuan.demo1.exception.VectorOperationException;
import com.hiyuan.demo1.repository.QaHistoryRepository;
import com.hiyuan.demo1.repository.UserRepository;
import com.hiyuan.demo1.repository.VectorRecordRepository;
import com.hiyuan.demo1.util.VectorUtils;
import dev.langchain4j.model.embedding.EmbeddingModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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

    /**
     * 处理问答请求
     */
    @Transactional
    public QaResponse ask(QaRequest request) {
        log.info("处理问答请求: {}", request.getQuestion());

        long startTime = System.currentTimeMillis();

        try {
            Optional<User> user = Optional.empty();
            if (request.getUserId() != null && !request.getUserId().isBlank()) {
                user = userRepository.findByUsername(request.getUserId().trim());
            }

            UUID userId = user.map(User::getId).orElse(null);

            int topK = request.getTopK() == null ? 5 : request.getTopK();
            if (topK < 1) topK = 1;
            if (topK > 20) topK = 20;

            // 生成问题向量并使用 MRL 截断
            float[] fullQuestionVector = embeddingModel.embed(request.getQuestion()).content().vector();
            float[] truncatedVector = mrlService.truncateVector(fullQuestionVector);
            String queryVector = VectorUtils.vectorToString(truncatedVector);
            
            log.debug("问题向量维度: {} -> {}", fullQuestionVector.length, truncatedVector.length);

            // 第一步：使用原生SQL查询最相似的向量ID（按相似度排序）
            List<UUID> nearestIds = vectorRecordRepository.findNearestVectorIds(queryVector, userId, topK);
            
            log.info("找到 {} 条相似向量ID", nearestIds.size());
            
            // 第二步：使用JPQL加载完整对象图（包含document和chunk）
            List<VectorRecord> nearest;
            if (nearestIds.isEmpty()) {
                nearest = List.of();
            } else {
                nearest = vectorRecordRepository.findByIdsWithRelations(nearestIds);
                log.info("加载完整对象后，记录数: {}", nearest.size());
                
                // 按照原始顺序排序（保持相似度顺序）
                Map<UUID, VectorRecord> recordMap = nearest.stream()
                    .collect(Collectors.toMap(VectorRecord::getId, vr -> vr));
                nearest = nearestIds.stream()
                    .map(recordMap::get)
                    .filter(Objects::nonNull)
                    .toList();
            }
            
            List<QaResponse.CitationInfo> citations = new ArrayList<>(nearest.size());

            for (VectorRecord vr : nearest) {
                Document document = vr.getDocument();
                DocumentChunk chunk = vr.getChunk();

                log.debug("VectorRecord ID: {}, Document: {}, Chunk: {}",
                    vr.getId(),
                    document != null ? document.getFilename() : "NULL",
                    chunk != null ? "存在" : "NULL");

                String content = chunk != null ? chunk.getContent() : null;
                if (content != null && content.length() > 500) {
                    content = content.substring(0, 500);
                }

                Double score = null;
                try {
                    float[] docVector = VectorUtils.parseVectorString(vr.getEmbedding());
                    score = VectorUtils.cosineSimilarity(truncatedVector, docVector);
                } catch (Exception ignored) {
                }

                citations.add(QaResponse.CitationInfo.builder()
                        .documentId(document == null ? null : document.getId().toString())
                        .documentTitle(document == null ? null : document.getFilename())
                        .content(content)
                        .score(score)
                        .build());
            }

            String prompt = buildPrompt(request.getQuestion(), citations);
            String answer = llmService.generate(prompt);

            QaHistory history = new QaHistory();
            history.setUser(user.orElse(null));
            history.setQuestion(request.getQuestion());
            history.setAnswer(answer);
            history.setModelVersion("ModelScope DeepSeek-R1");
            history.setResponseTime((int) (System.currentTimeMillis() - startTime));

            try {
                history = qaHistoryRepository.save(history);
            } catch (Exception e) {
                log.warn("保存问答历史失败，但不影响问答功能: {}", e.getMessage());
                history = null;
            }

            return QaResponse.builder()
                    .answer(answer)
                    .citations(citations)
                    .historyId(history == null ? null : history.getId().toString())
                    .build();

        } catch (BusinessException e) {
            // 业务异常直接抛出
            throw e;
        } catch (Exception e) {
            log.error("问答处理失败: {}", e.getMessage(), e);
            throw BusinessException.internalError("问答处理", e);
        }
    }

    /**
     * 构建提示词
     */
    private String buildPrompt(String question, List<QaResponse.CitationInfo> citations) {
        StringBuilder prompt = new StringBuilder();
        
        // 系统指令：要求简洁回答
        prompt.append("你是一个基于文档的教育者。回答要求：\n");
        prompt.append("1. 直接回答问题，不要分析过程\n");
        prompt.append("2. 以txt格式回答，格式要清晰美观\n");
        prompt.append("3. 如果不知道就说不知道，不要编造\n");
        prompt.append("4. 你现在是基于文档的教育者，请你回答时候通俗易懂，深入浅出\n\n");
        
        if (citations.isEmpty()) {
            prompt.append("问题：").append(question);
        } else {
            prompt.append("参考资料：\n");
            for (int i = 0; i < citations.size(); i++) {
                prompt.append(String.format("[%d] %s\n", i + 1, citations.get(i).getContent()));
            }
            prompt.append("\n问题：").append(question);
        }
        
        return prompt.toString();
    }
}
