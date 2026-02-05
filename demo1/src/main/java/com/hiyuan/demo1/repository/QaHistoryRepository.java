package com.hiyuan.demo1.repository;

import com.hiyuan.demo1.entity.QaHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
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

    /**
     * 获取用户最后一次提问的时间
     * 
     * 用途：获取学生的最后活动时间
     * 
     * @param userId 用户ID
     * @return 最后提问时间的Optional包装，如果没有记录则为空
     */
    @Query("SELECT MAX(q.askedAt) FROM QaHistory q WHERE q.user.id = :userId")
    Optional<LocalDateTime> findLastActivityByUserId(@Param("userId") UUID userId);

    /**
     * 统计用户在指定时间之后的提问数量
     * 
     * 用途：计算学生最近30天的提问数
     * 
     * @param userId 用户ID
     * @param since 起始时间
     * @return 提问数量
     */
    @Query("SELECT COUNT(q) FROM QaHistory q WHERE q.user.id = :userId AND q.askedAt >= :since")
    long countByUserIdAndAskedAtAfter(@Param("userId") UUID userId, @Param("since") LocalDateTime since);

    /**
     * 根据用户ID和日期范围查询问答历史（分页）
     * 
     * 用途：教师查看学生在指定日期范围内的问答历史
     * 
     * @param userId 用户ID
     * @param startDate 开始日期（包含）
     * @param endDate 结束日期（包含）
     * @param pageable 分页参数
     * @return 分页的问答历史记录
     */
    @Query("SELECT q FROM QaHistory q WHERE q.user.id = :userId " +
            "AND (:startDate IS NULL OR q.askedAt >= :startDate) " +
            "AND (:endDate IS NULL OR q.askedAt <= :endDate)")
    Page<QaHistory> findByUserIdAndDateRange(
            @Param("userId") UUID userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    /**
     * 统计用户在指定日期范围内的提问数量
     * 
     * 用途：导出学生报表时，统计指定日期范围内的提问数
     * 
     * @param userId 用户ID
     * @param startDate 开始日期（包含，可为null表示不限制开始时间）
     * @param endDate 结束日期（包含，可为null表示不限制结束时间）
     * @return 提问数量
     */
    @Query("SELECT COUNT(q) FROM QaHistory q WHERE q.user.id = :userId " +
            "AND (:startDate IS NULL OR q.askedAt >= :startDate) " +
            "AND (:endDate IS NULL OR q.askedAt <= :endDate)")
    long countByUserIdAndDateRange(
            @Param("userId") UUID userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}
