package com.hiyuan.demo1.repository;

import com.hiyuan.demo1.entity.Document;
import com.hiyuan.demo1.enums.DocumentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 文档数据访问接口
 */
@Repository
public interface DocumentRepository extends JpaRepository<Document, UUID> {

    /**
     * 根据用户 ID 查找所有文档
     */
    List<Document> findByUserId(UUID userId);

    /**
     * 根据用户 ID 分页查找文档
     */
    Page<Document> findByUserId(UUID userId, Pageable pageable);

    /**
     * 根据用户 ID 和状态查找文档
     */
    List<Document> findByUserIdAndStatus(UUID userId, DocumentStatus status);

    /**
     * 根据用户 ID 和文件名查找文档
     */
    Optional<Document> findByUserIdAndFilename(UUID userId, String filename);

    /**
     * 统计用户的文档数量
     */
    long countByUserId(UUID userId);

    /**
     * 统计用户成功处理的文档数量
     */
    long countByUserIdAndStatus(UUID userId, DocumentStatus status);

    /**
     * 查找所有需要处理的文档
     */
    @Query("SELECT d FROM Document d WHERE d.status = :status ORDER BY d.uploadedAt ASC")
    List<Document> findDocumentsToProcess(@Param("status") DocumentStatus status);
}
