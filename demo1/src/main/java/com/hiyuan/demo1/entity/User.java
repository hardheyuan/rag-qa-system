package com.hiyuan.demo1.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户实体类 - 映射数据库中的 t_user 表
 * 
 * 这个类代表系统中的用户，包含用户的基本信息和关联数据
 * 
 * 主要功能：
 * 1. 存储用户基本信息（用户名、邮箱、密码）
 * 2. 管理用户状态（是否激活）
 * 3. 关联用户的文档和问答历史
 * 
 * 数据库表结构：
 * - id: UUID主键（继承自BaseEntity）
 * - username: 用户名，唯一索引
 * - email: 邮箱，唯一索引，可为空
 * - password: 密码，加密存储
 * - is_active: 是否激活，默认true
 * - created_at: 创建时间（继承自BaseEntity）
 * - updated_at: 更新时间（继承自BaseEntity）
 * 
 * 关联关系：
 * - 一个用户可以有多个文档 (1:N)
 * - 一个用户可以有多个问答历史 (1:N)
 * 
 * @author 开发团队
 * @version 1.0.0
 */
@Entity  // JPA注解：标识这是一个数据库实体类
@Table(name = "t_user")  // 指定对应的数据库表名
@Getter  // Lombok：自动生成所有字段的getter方法
@Setter  // Lombok：自动生成所有字段的setter方法
@NoArgsConstructor  // Lombok：生成无参构造函数
@AllArgsConstructor // Lombok：生成全参构造函数
@Builder // Lombok：生成建造者模式的代码，方便创建对象
public class User extends BaseEntity {

    /**
     * 用户名
     * 
     * 业务规则：
     * - 必填字段，不能为空
     * - 全局唯一，不能重复
     * - 最大长度100字符
     * - 用于用户登录和识别
     * 
     * 数据库约束：
     * - NOT NULL: 不能为空
     * - UNIQUE: 唯一约束，确保不重复
     * - LENGTH: 最大100字符
     */
    @Column(name = "username", nullable = false, unique = true, length = 100)
    private String username;

    /**
     * 邮箱地址
     * 
     * 业务规则：
     * - 可选字段，可以为空
     * - 如果填写，必须全局唯一
     * - 最大长度100字符
     * - 用于找回密码、发送通知等
     * 
     * 数据库约束：
     * - UNIQUE: 唯一约束，但允许为NULL
     * - LENGTH: 最大100字符
     */
    @Column(name = "email", unique = true, length = 100)
    private String email;

    /**
     * 密码
     * 
     * 安全要求：
     * - 存储时必须加密（建议使用BCrypt）
     * - 最大长度255字符（足够存储加密后的密码）
     * - 不能明文存储
     * 
     * 注意：当前版本为简化实现，建议后续补齐：
     * 1. 密码加密存储
     * 2. 密码强度验证
     * 3. 密码过期策略
     */
    @Column(name = "password", length = 255)
    private String password;

    /**
     * 用户角色
     * 
     * 用途：
     * - 定义用户权限级别
     * - 支持三种角色：学生、教师、管理员
     * - 默认角色为学生
     * 
     * 角色权限等级：
     * - STUDENT: 学生，可提问和查看文档
     * - TEACHER: 教师，可上传和管理文档
     * - ADMIN: 管理员，拥有所有权限
     * 
     * 注意：数据库层面允许null以兼容现有数据，
     * 但程序逻辑会通过@Builder.Default确保新用户默认值为STUDENT
     */
    @Column(name = "role", length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private UserRole role = UserRole.STUDENT;

    /**
     * 用户激活状态
     * 
     * 用途：
     * - 控制用户是否可以正常使用系统
     * - 可用于用户封禁、临时禁用等场景
     * - 默认为true（激活状态）
     * 
     * 业务逻辑：
     * - true: 用户可以正常登录和使用系统
     * - false: 用户被禁用，无法登录
     */
    @Column(name = "is_active")
    @Builder.Default  // Lombok Builder模式的默认值
    private Boolean isActive = true;

    /**
     * 用户上传的文档列表
     * 
     * 关联关系：一对多 (One-to-Many)
     * - 一个用户可以上传多个文档
     * - 当用户被删除时，其所有文档也会被删除
     * 
     * JPA注解说明：
     * @OneToMany - 一对多关系
     * mappedBy = "user" - 关联关系由Document实体中的user字段维护
     * cascade = CascadeType.ALL - 级联操作：用户的增删改会影响关联的文档
     * orphanRemoval = true - 孤儿删除：如果文档不再关联任何用户，自动删除
     * 
     * 使用场景：
     * - 查询用户的所有文档：user.getDocuments()
     * - 统计用户文档数量：user.getDocuments().size()
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<Document> documents = new ArrayList<>();

    /**
     * 用户的问答历史记录
     * 
     * 关联关系：一对多 (One-to-Many)
     * - 一个用户可以有多条问答记录
     * - 当用户被删除时，其所有问答历史也会被删除
     * 
     * 使用场景：
     * - 查询用户的问答历史：user.getQaHistories()
     * - 统计用户提问次数：user.getQaHistories().size()
     * - 分析用户使用习惯
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<QaHistory> qaHistories = new ArrayList<>();
    
    /*
     * 使用示例：
     * 
     * 1. 创建新用户：
     * User user = User.builder()
     *     .username("张三")
     *     .email("zhangsan@example.com")
     *     .password(passwordEncoder.encode("123456"))  // 密码加密
     *     .isActive(true)
     *     .build();
     * userRepository.save(user);
     * 
     * 2. 查询用户及其文档：
     * User user = userRepository.findById(userId).orElse(null);
     * List<Document> userDocs = user.getDocuments();  // 获取用户的所有文档
     * 
     * 3. 统计用户数据：
     * int docCount = user.getDocuments().size();      // 文档数量
     * int qaCount = user.getQaHistories().size();     // 问答次数
     * 
     * 4. 用户状态管理：
     * user.setIsActive(false);  // 禁用用户
     * userRepository.save(user);
     * 
     * 5. 删除用户（会级联删除相关数据）：
     * userRepository.delete(user);  // 自动删除用户的文档和问答历史
     */
}
