package com.hiyuan.demo1.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 向量记录实体
 * 映射 t_vector_record 表
 * 
 * 注意：embedding 字段使用 pgvector 的 vector 类型
 * 由于 JPA 不直接支持 vector 类型，这里使用 String 存储
 * 实际的向量操作通过原生 SQL 查询执行
 */
@Entity
@Table(name = "t_vector_record")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VectorRecord extends BaseEntity {

    /**
     * 关联的文档分块（一对一）
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chunk_id", nullable = false, unique = true)
    private DocumentChunk chunk;

    /**
     * 所属文档
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    /**
     * 向量数据
     * 存储为字符串格式: "[0.1, 0.2, 0.3, ...]"
     * 实际查询时通过 pgvector 原生 SQL 操作
     */
    @Column(name = "embedding", columnDefinition = "vector(1024)")
    private String embedding;

    /**
     * 向量维度
     */
    @Column(name = "embedding_dim")
    @Builder.Default
    private Integer embeddingDim = 1024;

    /**
     * 向量化模型名称
     */
    @Column(name = "embedding_model", length = 100)
    private String embeddingModel;
}
