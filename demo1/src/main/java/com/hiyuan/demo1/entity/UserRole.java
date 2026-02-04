package com.hiyuan.demo1.entity;

import lombok.Getter;

/**
 * 用户角色枚举
 * 定义系统中的三种用户角色及其权限等级
 * 
 * 角色权限等级（从低到高）：
 * STUDENT < TEACHER < ADMIN
 * 
 * 角色说明：
 * - STUDENT: 学生角色，只能查看文档和提问
 * - TEACHER: 教师角色，可以上传文档、管理自己的文档
 * - ADMIN: 管理员角色，拥有所有权限，可以管理所有用户和系统设置
 * 
 * @author 开发团队
 * @version 1.0.0
 */
@Getter
public enum UserRole {
    
    /**
     * 学生角色
     * 权限：
     * - 查看和搜索文档
     * - 向AI助手提问
     * - 查看自己的问答历史
     */
    STUDENT("学生", "Student", 1),
    
    /**
     * 教师角色
     * 权限：
     * - 学生所有权限
     * - 上传、删除、管理文档
     * - 查看文档统计信息
     */
    TEACHER("教师", "Teacher", 2),
    
    /**
     * 管理员角色
     * 权限：
     * - 教师和学生的所有权限
     * - 管理所有用户（启用/禁用/删除）
     * - 系统设置管理
     * - 查看全局统计数据
     */
    ADMIN("管理员", "Admin", 3);
    
    private final String chineseName;
    private final String englishName;
    private final int level;
    
    UserRole(String chineseName, String englishName, int level) {
        this.chineseName = chineseName;
        this.englishName = englishName;
        this.level = level;
    }
    
    /**
     * 检查当前角色是否有权限访问目标角色资源
     * 
     * @param requiredRole 需要的最低角色权限
     * @return 是否有权限
     */
    public boolean hasRole(UserRole requiredRole) {
        return this.level >= requiredRole.level;
    }
    
    /**
     * 从字符串解析角色
     * 
     * @param role 角色名称（支持中文名、英文名或大写英文名）
     * @return UserRole 枚举值
     * @throws IllegalArgumentException 如果无法解析
     */
    public static UserRole fromString(String role) {
        if (role == null || role.trim().isEmpty()) {
            return STUDENT; // 默认角色
        }
        
        String normalized = role.trim().toUpperCase();
        
        for (UserRole userRole : values()) {
            if (userRole.name().equals(normalized) ||
                userRole.getEnglishName().equalsIgnoreCase(role.trim()) ||
                userRole.getChineseName().equals(role.trim())) {
                return userRole;
            }
        }
        
        throw new IllegalArgumentException("Unknown role: " + role);
    }
    
    /**
     * 获取Spring Security的角色名称（带ROLE_前缀）
     * 
     * @return ROLE_XXX 格式的角色名
     */
    public String getSpringSecurityRole() {
        return "ROLE_" + this.name();
    }
}
