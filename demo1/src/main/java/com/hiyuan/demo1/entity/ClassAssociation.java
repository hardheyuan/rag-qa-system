package com.hiyuan.demo1.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 班级关联实体类 - 映射数据库中的 t_class_association 表
 * 
 * 这个类代表教师与学生之间的关联关系，用于管理教师的班级成员
 * 
 * 主要功能：
 * 1. 建立教师与学生之间的多对多关系
 * 2. 记录学生加入班级的时间
 * 3. 支持教师管理自己班级的学生
 * 
 * 数据库表结构：
 * - id: UUID主键（继承自BaseEntity）
 * - teacher_id: 教师用户ID，外键关联t_user表
 * - student_id: 学生用户ID，外键关联t_user表
 * - enrolled_at: 学生加入班级的时间
 * - created_at: 创建时间（继承自BaseEntity）
 * - updated_at: 更新时间（继承自BaseEntity）
 * 
 * 约束：
 * - 唯一约束：同一教师不能重复添加同一学生
 * - 外键约束：级联删除，当用户被删除时关联记录也会被删除
 * 
 * @author 开发团队
 * @version 1.0.0
 */
@Entity  // JPA注解：标识这是一个数据库实体类
@Table(name = "t_class_association", 
       uniqueConstraints = @UniqueConstraint(
           name = "uk_teacher_student",
           columnNames = {"teacher_id", "student_id"}
       ))
@Getter  // Lombok：自动生成所有字段的getter方法
@Setter  // Lombok：自动生成所有字段的setter方法
@NoArgsConstructor  // Lombok：生成无参构造函数
@AllArgsConstructor // Lombok：生成全参构造函数
@Builder // Lombok：生成建造者模式的代码，方便创建对象
public class ClassAssociation extends BaseEntity {

    /**
     * 教师用户
     * 
     * 关联关系：多对一 (Many-to-One)
     * - 多个班级关联可以属于同一个教师
     * - 一个教师可以有多个学生
     * 
     * JPA注解说明：
     * @ManyToOne - 多对一关系
     * fetch = FetchType.LAZY - 延迟加载，只有在访问时才查询数据库
     * @JoinColumn - 指定外键列
     *   - name: 数据库列名
     *   - nullable: false 表示不能为空
     * 
     * 业务规则：
     * - 必须是具有TEACHER角色的用户
     * - 教师可以管理自己班级的学生
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;

    /**
     * 学生用户
     * 
     * 关联关系：多对一 (Many-to-One)
     * - 多个班级关联可以关联到同一个学生
     * - 一个学生可以属于多个教师的班级
     * 
     * JPA注解说明：
     * @ManyToOne - 多对一关系
     * fetch = FetchType.LAZY - 延迟加载，只有在访问时才查询数据库
     * @JoinColumn - 指定外键列
     *   - name: 数据库列名
     *   - nullable: false 表示不能为空
     * 
     * 业务规则：
     * - 必须是具有STUDENT角色的用户
     * - 学生可以被多个教师添加到各自的班级
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    /**
     * 加入班级时间
     * 
     * 用途：
     * - 记录学生何时被添加到教师的班级
     * - 用于按加入时间排序学生列表
     * - 默认为当前时间
     * 
     * 业务规则：
     * - 创建关联时自动设置为当前时间
     * - 可用于统计和报表生成
     */
    @Column(name = "enrolled_at")
    @Builder.Default
    private LocalDateTime enrolledAt = LocalDateTime.now();
    
    /*
     * 使用示例：
     * 
     * 1. 创建新的班级关联：
     * ClassAssociation association = ClassAssociation.builder()
     *     .teacher(teacherUser)
     *     .student(studentUser)
     *     .build();  // enrolledAt 会自动设置为当前时间
     * classAssociationRepository.save(association);
     * 
     * 2. 查询教师的所有学生：
     * List<ClassAssociation> associations = classAssociationRepository.findByTeacherId(teacherId);
     * List<User> students = associations.stream()
     *     .map(ClassAssociation::getStudent)
     *     .collect(Collectors.toList());
     * 
     * 3. 检查关联是否存在：
     * boolean exists = classAssociationRepository.existsByTeacherIdAndStudentId(teacherId, studentId);
     * 
     * 4. 删除关联（不会删除用户账号）：
     * classAssociationRepository.deleteByTeacherIdAndStudentId(teacherId, studentId);
     */
}
