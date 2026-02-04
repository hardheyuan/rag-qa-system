package com.hiyuan.demo1.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 基础实体类 - 所有数据库实体的父类
 * 
 * 这个类定义了所有数据库表都需要的通用字段：
 * 1. id - 主键，使用UUID确保全局唯一
 * 2. createdAt - 创建时间，记录数据何时被创建
 * 3. updatedAt - 更新时间，记录数据最后修改时间
 * 
 * 为什么使用基础实体类？
 * - 避免重复代码：每个实体都需要这些字段
 * - 统一规范：确保所有表都有相同的基础字段
 * - 便于维护：如果需要添加新的通用字段，只需修改这里
 * 
 * 为什么使用UUID而不是自增ID？
 * - 全局唯一：即使在分布式系统中也不会重复
 * - 安全性：不会暴露数据量信息（自增ID可以推测出总记录数）
 * - 便于数据迁移：不同数据库之间迁移时不会冲突
 * 
 * @author 开发团队
 * @version 1.0.0
 */
@Getter  // Lombok注解：自动生成所有字段的getter方法
@Setter  // Lombok注解：自动生成所有字段的setter方法
@MappedSuperclass  // JPA注解：表示这是一个映射的超类，不会创建单独的表
public abstract class BaseEntity {

    /**
     * 主键ID
     * 
     * 使用UUID作为主键的优势：
     * - 全局唯一：128位随机数，重复概率极低
     * - 分布式友好：多个服务器同时生成也不会冲突
     * - 安全性：外部无法猜测ID规律
     * 
     * 注解说明：
     * @Id - 标识这是主键字段
     * @GeneratedValue - 自动生成值，策略为UUID
     * @Column - 数据库列配置
     *   - name: 数据库列名
     *   - updatable: false 表示更新时不能修改ID
     *   - nullable: false 表示不能为空
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)  // 自动生成UUID
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /**
     * 创建时间
     * 
     * 记录这条数据是什么时候创建的
     * 用途：
     * - 数据审计：知道数据的创建时间
     * - 排序：按创建时间排序
     * - 统计：分析数据创建趋势
     * 
     * @CreationTimestamp - Hibernate注解，自动设置创建时间
     * updatable = false - 创建后不能修改
     */
    @CreationTimestamp  // 创建时自动设置当前时间
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     * 
     * 记录这条数据最后一次被修改的时间
     * 用途：
     * - 数据审计：知道数据的最后修改时间
     * - 缓存策略：根据更新时间判断是否需要刷新缓存
     * - 同步：在数据同步时判断哪些数据需要更新
     * 
     * @UpdateTimestamp - Hibernate注解，每次更新时自动设置当前时间
     */
    @UpdateTimestamp   // 每次更新时自动设置当前时间
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /*
     * 使用示例：
     * 
     * 当你创建一个新的实体类时，只需要继承BaseEntity：
     * 
     * @Entity
     * @Table(name = "t_user")
     * public class User extends BaseEntity {
     *     private String username;
     *     private String email;
     *     // 自动拥有 id, createdAt, updatedAt 字段
     * }
     * 
     * 保存数据时：
     * User user = new User();
     * user.setUsername("张三");
     * userRepository.save(user);
     * // id 会自动生成
     * // createdAt 会自动设置为当前时间
     * // updatedAt 会自动设置为当前时间
     * 
     * 更新数据时：
     * user.setUsername("李四");
     * userRepository.save(user);
     * // id 和 createdAt 保持不变
     * // updatedAt 会自动更新为当前时间
     */
}
