package com.hiyuan.demo1.repository;

import com.hiyuan.demo1.entity.VectorRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 向量记录数据访问接口
 * 
 * 注意：向量相似度查询需要使用原生 SQL
 */
@Repository
public interface VectorRecordRepository extends JpaRepository<VectorRecord, UUID> {

    /**
     * 使用原生 SQL 插入向量记录
     * JPA 不支持 pgvector 类型，必须使用原生 SQL
     */
    @Modifying
    @Query(value = """
            INSERT INTO t_vector_record (id, chunk_id, document_id, embedding, embedding_dim, embedding_model, created_at, updated_at)
            VALUES (:id, :chunkId, :documentId, CAST(:embedding AS vector), :embeddingDim, :embeddingModel, NOW(), NOW())
            """, nativeQuery = true)
    void insertWithVector(
            @Param("id") UUID id,
            @Param("chunkId") UUID chunkId,
            @Param("documentId") UUID documentId,
            @Param("embedding") String embedding,
            @Param("embeddingDim") int embeddingDim,
            @Param("embeddingModel") String embeddingModel);

    /**
     * 根据分块 ID 查找向量记录
     */
    Optional<VectorRecord> findByChunkId(UUID chunkId);

    /**
     * 根据文档 ID 查找所有向量记录
     */
    List<VectorRecord> findByDocumentId(UUID documentId);

    /**
     * 删除文档的所有向量记录
     */
    void deleteByDocumentId(UUID documentId);

    /**
     * 统计文档的向量记录数量
     */
    long countByDocumentId(UUID documentId);

    /**
     * 向量相似度查询（原生 SQL）
     * 使用 pgvector 的 <=> 操作符计算 cosine 距离
     * 返回向量记录的 ID 列表，需要后续查询加载完整对象
     *
     * @param queryVector 查询向量（字符串格式）
     * @param userId      用户 ID（用于数据隔离；为 null 时不做用户过滤）
     * @param limit       返回数量
     * @return 最相似的向量记录 ID 列表
     */
    @Query(value = """
            SELECT vr.id FROM t_vector_record vr
            JOIN t_document d ON vr.document_id = d.id
            WHERE (CAST(:userId AS UUID) IS NULL OR d.user_id = :userId)
              AND d.status = 'SUCCESS'
            ORDER BY vr.embedding <=> CAST(:queryVector AS vector)
            LIMIT :limit
            """, nativeQuery = true)
    List<UUID> findNearestVectorIds(
            @Param("queryVector") String queryVector,
            @Param("userId") UUID userId,
            @Param("limit") int limit);

    /**
     * 按文档拥有者列表进行向量相似度查询
     */
    @Query(value = """
            SELECT vr.id FROM t_vector_record vr
            JOIN t_document d ON vr.document_id = d.id
            WHERE d.user_id IN (:ownerIds)
              AND d.status = 'SUCCESS'
            ORDER BY vr.embedding <=> CAST(:queryVector AS vector)
            LIMIT :limit
            """, nativeQuery = true)
    List<UUID> findNearestVectorIdsByOwnerIds(
            @Param("queryVector") String queryVector,
            @Param("ownerIds") List<UUID> ownerIds,
            @Param("limit") int limit);
    
    /**
     * 根据ID列表查询向量记录（带关联对象，保持顺序）
     * 用于在原生SQL查询后加载完整对象图
     */
    @Query("SELECT vr FROM VectorRecord vr " +
           "JOIN FETCH vr.document d " +
           "JOIN FETCH vr.chunk c " +
           "WHERE vr.id IN :ids")
    List<VectorRecord> findByIdsWithRelations(@Param("ids") List<UUID> ids);
}
