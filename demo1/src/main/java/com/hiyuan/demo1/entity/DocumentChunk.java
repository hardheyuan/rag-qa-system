package com.hiyuan.demo1.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;

/**
 * 文档分块实体
 * 映射 t_document_chunk 表
 */
@Entity
@Table(name = "t_document_chunk", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "document_id", "chunk_index" })
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentChunk extends BaseEntity {

    /**
     * 所属文档
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    /**
     * 分块索引（从 0 开始）
     */
    @Column(name = "chunk_index", nullable = false)
    private Integer chunkIndex;

    /**
     * 分块文本内容
     */
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * 内容长度（字符数）
     */
    @Column(name = "content_length")
    private Integer contentLength;

    /**
     * 页码（PDF 文档）
     */
    @Column(name = "page_num")
    private Integer pageNum;

    /**
     * 章节标题
     */
    @Column(name = "section_title", length = 255)
    private String sectionTitle;

    /**
     * 原文本中的起始字符位置
     */
    @Column(name = "char_start")
    private Integer charStart;

    /**
     * 原文本中的结束字符位置
     */
    @Column(name = "char_end")
    private Integer charEnd;

    /**
     * 元数据（JSON 格式）
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    /**
     * 关联的向量记录（一对一）
     */
    @OneToOne(mappedBy = "chunk", cascade = CascadeType.ALL, orphanRemoval = true)
    private VectorRecord vectorRecord;
    
    /**
     * JPA 钩子：在保存到数据库前自动清理非法字符
     * 
     * 这是最后一道防线，确保即使通过 Builder 或其他方式创建的对象
     * 在持久化前也会被清理
     */
    @PrePersist
    @PreUpdate
    public void prePersist() {
        this.content = cleanForPostgres(this.content);
        this.sectionTitle = cleanForPostgres(this.sectionTitle);
    }
    
    /**
     * 深度清理字符串，移除所有 PostgreSQL UTF-8 不支持的字符
     * 
     * 包括：控制字符、空字符、BOM标记等
     */
    private String cleanForPostgres(String input) {
        if (input == null) {
            return null;
        }
        return input
            .replaceAll("[\\u0000-\\u0008]", "")   // 控制字符 0x00-0x08
            .replaceAll("[\\u000B-\\u000C]", "")   // 垂直制表符和换页符
            .replaceAll("[\\u000E-\\u001F]", "")   // 控制字符 0x0E-0x1F
            .replaceAll("[\\u007F-\\u009F]", "")   // DEL 和扩展控制字符
            .replaceAll("\\uFEFF", "");             // BOM 标记
    }
}