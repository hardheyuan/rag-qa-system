package com.hiyuan.demo1.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * 用户列表响应DTO
 * 
 * 用于前端用户管理页面显示的用户信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserListResponse {

    /**
     * 用户ID
     */
    private UUID id;

    /**
     * 用户姓名（用户名）
     */
    private String name;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 角色
     */
    private String role;

    /**
     * 状态 (Active/Inactive)
     */
    private String status;

    /**
     * 最后登录时间
     */
    private String lastLogin;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 所属部门
     */
    private String department;

    /**
     * 从User实体构建响应DTO
     */
    public static UserListResponse fromUser(com.hiyuan.demo1.entity.User user) {
        return UserListResponse.builder()
                .id(user.getId())
                .name(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole() != null ? user.getRole().name() : "STUDENT")
                .status(user.getIsActive() != null && user.getIsActive() ? "Active" : "Inactive")
                .lastLogin("-")  // 暂时不记录最后登录时间
                .avatar("")
                .department("")
                .build();
    }
}
