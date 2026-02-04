package com.hiyuan.demo1.repository;

import com.hiyuan.demo1.entity.QaHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * 问答历史数据访问接口
 */
@Repository
public interface QaHistoryRepository extends JpaRepository<QaHistory, UUID> {

    /**
     * 根据用户 ID 查找问答历史（按时间倒序）
     */
    List<QaHistory> findByUserIdOrderByAskedAtDesc(UUID userId);

    /**
     * 根据用户 ID 分页查找问答历史
     */
    Page<QaHistory> findByUserId(UUID userId, Pageable pageable);

    /**
     * 根据用户 ID 和关键词搜索问答历史
     */
    @Query("SELECT q FROM QaHistory q WHERE q.user.id = :userId AND " +
            "(LOWER(q.question) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(q.answer) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "ORDER BY q.askedAt DESC")
    Page<QaHistory> searchByKeyword(
            @Param("userId") UUID userId,
            @Param("keyword") String keyword,
            Pageable pageable);

    /**
     * 统计用户的问答数量
     */
    long countByUserId(UUID userId);

    /**
     * 删除用户的所有问答历史
     */
    void deleteByUserId(UUID userId);
}
