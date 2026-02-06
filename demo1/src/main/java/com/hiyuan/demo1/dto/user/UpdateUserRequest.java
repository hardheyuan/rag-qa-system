package com.hiyuan.demo1.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新用户请求DTO
 *
 * 用于管理员更新用户信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUserRequest {

    /**
     * 用户姓名
     */
    private String name;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 角色 (ADMIN/TEACHER/STUDENT)
     */
    private String role;

    /**
     * 状态 (Active/Inactive)
     */
    private String status;

    /**
     * 所属部门
     */
    private String department;
}
