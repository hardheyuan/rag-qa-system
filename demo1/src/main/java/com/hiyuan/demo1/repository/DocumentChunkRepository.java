package com.hiyuan.demo1.repository;

import com.hiyuan.demo1.entity.DocumentChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 文档分块数据访问接口
 */
@Repository
public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, UUID> {

    /**
     * 根据文档 ID 查找所有分块
     */
    List<DocumentChunk> findByDocumentIdOrderByChunkIndex(UUID documentId);

    /**
     * 根据文档 ID 和分块索引查找
     */
    Optional<DocumentChunk> findByDocumentIdAndChunkIndex(UUID documentId, Integer chunkIndex);

    /**
     * 统计文档的分块数量
     */
    long countByDocumentId(UUID documentId);

    /**
     * 删除文档的所有分块
     */
    void deleteByDocumentId(UUID documentId);
}
