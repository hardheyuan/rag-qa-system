package com.hiyuan.demo1.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 向量存储服务 - 处理向量数据的持久化
 * 
 * 由于 JPA 不支持 pgvector 类型，需要使用原生 SQL 插入向量数据
 */
@Slf4j
@Service
public class VectorStorageService {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * 插入向量记录到数据库
     * 使用原生 SQL 和 CAST 将字符串转换为 vector 类型
     */
    @Transactional
    public void insertVectorRecord(UUID id, UUID chunkId, UUID documentId, 
                                   String embedding, int embeddingDim, String embeddingModel) {
        log.debug("插入向量记录: chunkId={}, dim={}", chunkId, embeddingDim);
        
        entityManager.createNativeQuery("""
            INSERT INTO t_vector_record (id, chunk_id, document_id, embedding, embedding_dim, embedding_model, created_at, updated_at)
            VALUES (:id, :chunkId, :documentId, CAST(:embedding AS vector), :embeddingDim, :embeddingModel, NOW(), NOW())
            """)
            .setParameter("id", id)
            .setParameter("chunkId", chunkId)
            .setParameter("documentId", documentId)
            .setParameter("embedding", embedding)
            .setParameter("embeddingDim", embeddingDim)
            .setParameter("embeddingModel", embeddingModel)
            .executeUpdate();
        
        log.debug("向量记录插入成功: id={}", id);
    }
}
