package com.hiyuan.demo1.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 引用来源实体
 * 映射 t_citation 表
 */
@Entity
@Table(name = "t_citation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Citation extends BaseEntity {

    /**
     * 所属问答记录
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "qa_id", nullable = false)
    private QaHistory qaHistory;

    /**
     * 引用的分块
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chunk_id", nullable = false)
    private DocumentChunk chunk;

    /**
     * 引用的文档
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    /**
     * 页码
     */
    @Column(name = "page_num")
    private Integer pageNum;

    /**
     * 分块索引
     */
    @Column(name = "chunk_index")
    private Integer chunkIndex;

    /**
     * 相关性评分（0-1）
     */
    @Column(name = "relevance_score")
    private Float relevanceScore;

    /**
     * 引用文本摘要
     */
    @Column(name = "citation_text", length = 500)
    private String citationText;
}
