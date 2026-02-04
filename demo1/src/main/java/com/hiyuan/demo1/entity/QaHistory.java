package com.hiyuan.demo1.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 问答历史实体
 * 映射 t_qa_history 表
 */
@Entity
@Table(name = "t_qa_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QaHistory extends BaseEntity {

    /**
     * 所属用户（可选）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    /**
     * 用户问题
     */
    @Column(name = "question", nullable = false, length = 1000)
    private String question;

    /**
     * LLM 生成的答案
     */
    @Column(name = "answer", nullable = false, columnDefinition = "TEXT")
    private String answer;

    /**
     * 响应时间（毫秒）
     */
    @Column(name = "response_time")
    private Integer responseTime;

    /**
     * 检索到的分块 ID（JSON 数组）
     */
    @Column(name = "retrieved_chunks", columnDefinition = "TEXT")
    private String retrievedChunks;

    /**
     * 检索到的文档 ID（JSON 数组）
     */
    @Column(name = "retrieved_documents", columnDefinition = "TEXT")
    private String retrievedDocuments;

    /**
     * 使用的模型版本
     */
    @Column(name = "model_version", length = 100)
    private String modelVersion;

    /**
     * 提问时间
     */
    @Column(name = "asked_at")
    @Builder.Default
    private LocalDateTime askedAt = LocalDateTime.now();

    /**
     * 关联的引用列表
     */
    @OneToMany(mappedBy = "qaHistory", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Citation> citations = new ArrayList<>();
}
