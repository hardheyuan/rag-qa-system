package com.hiyuan.demo1.repository;

import com.hiyuan.demo1.entity.Citation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * 引用来源数据访问接口
 */
@Repository
public interface CitationRepository extends JpaRepository<Citation, UUID> {

    /**
     * 根据问答 ID 查找所有引用（按相关性评分倒序）
     */
    List<Citation> findByQaHistoryIdOrderByRelevanceScoreDesc(UUID qaHistoryId);

    /**
     * 根据问答 ID 查找所有引用
     */
    List<Citation> findByQaHistoryId(UUID qaHistoryId);

    /**
     * 统计问答的引用数量
     */
    long countByQaHistoryId(UUID qaHistoryId);

    /**
     * 删除问答的所有引用
     */
    void deleteByQaHistoryId(UUID qaHistoryId);
}
