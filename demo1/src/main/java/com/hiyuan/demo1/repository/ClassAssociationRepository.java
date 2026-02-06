package com.hiyuan.demo1.repository;

import com.hiyuan.demo1.entity.ClassAssociation;
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
 * 班级关联数据访问接口
 * 
 * 提供教师-学生关联关系的数据库操作方法
 * 
 * 主要功能：
 * 1. 查询教师的所有学生关联
 * 2. 检查关联是否存在
 * 3. 查找和删除特定关联
 * 4. 统计教师的学生数量
 * 5. 支持分页查询
 * 
 * @author 开发团队
 * @version 1.0.0
 * @see ClassAssociation
 * 
 * Requirements: 1.5, 2.1
 */
@Repository
public interface ClassAssociationRepository extends JpaRepository<ClassAssociation, UUID> {

    /**
     * 查询教师的所有学生关联
     * 
     * 用途：获取教师班级中所有学生的关联记录
     * 
     * @param teacherId 教师用户ID
     * @return 该教师的所有学生关联列表
     * 
     * 使用示例：
     * List<ClassAssociation> associations = repository.findByTeacherId(teacherId);
     */
    List<ClassAssociation> findByTeacherId(UUID teacherId);

    /**
     * 查询教师的所有学生关联（分页）
     * 
     * 用途：分页获取教师班级中的学生关联记录，支持大量数据的高效查询
     * 
     * @param teacherId 教师用户ID
     * @param pageable 分页参数（页码、每页大小、排序）
     * @return 分页的学生关联数据
     * 
     * 使用示例：
     * Pageable pageable = PageRequest.of(0, 20, Sort.by("enrolledAt").descending());
     * Page<ClassAssociation> page = repository.findByTeacherId(teacherId, pageable);
     */
    Page<ClassAssociation> findByTeacherId(UUID teacherId, Pageable pageable);

    /**
     * 检查关联是否存在
     * 
     * 用途：
     * - 验证教师是否有权访问某个学生
     * - 检查是否重复添加学生
     * 
     * @param teacherId 教师用户ID
     * @param studentId 学生用户ID
     * @return true 如果关联存在，false 如果不存在
     * 
     * 使用示例：
     * if (repository.existsByTeacherIdAndStudentId(teacherId, studentId)) {
     *     // 关联已存在，不能重复添加
     * }
     */
    boolean existsByTeacherIdAndStudentId(UUID teacherId, UUID studentId);

    /**
     * 查找特定关联
     * 
     * 用途：获取教师与特定学生之间的关联详情
     * 
     * @param teacherId 教师用户ID
     * @param studentId 学生用户ID
     * @return 关联记录的Optional包装，如果不存在则为空
     * 
     * 使用示例：
     * Optional<ClassAssociation> association = repository.findByTeacherIdAndStudentId(teacherId, studentId);
     * association.ifPresent(a -> {
     *     LocalDateTime enrolledAt = a.getEnrolledAt();
     * });
     */
    Optional<ClassAssociation> findByTeacherIdAndStudentId(UUID teacherId, UUID studentId);

    /**
     * 删除特定关联
     * 
     * 用途：从教师班级中移除学生（不会删除学生账号）
     * 
     * 注意：此方法只删除关联记录，不会影响：
     * - 学生的User账号
     * - 学生的学习记录（QaHistory等）
     * 
     * @param teacherId 教师用户ID
     * @param studentId 学生用户ID
     * 
     * 使用示例：
     * repository.deleteByTeacherIdAndStudentId(teacherId, studentId);
     */
    void deleteByTeacherIdAndStudentId(UUID teacherId, UUID studentId);

    /**
     * 统计教师的学生数量
     * 
     * 用途：获取教师班级中的学生总数，用于统计和显示
     * 
     * @param teacherId 教师用户ID
     * @return 该教师班级中的学生数量
     * 
     * 使用示例：
     * long studentCount = repository.countByTeacherId(teacherId);
     */
    long countByTeacherId(UUID teacherId);

    /**
     * 查询学生关联的教师 ID 列表
     */
    @Query("SELECT ca.teacher.id FROM ClassAssociation ca WHERE ca.student.id = :studentId")
    List<UUID> findTeacherIdsByStudentId(@Param("studentId") UUID studentId);
}
