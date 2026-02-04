package com.hiyuan.demo1.dto.auth;

import com.hiyuan.demo1.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * 用户信息响应 DTO
 * 
 * 返回给前端的基本用户信息
 * 
 * @author 开发团队
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoResponse {

    private UUID id;
    private String username;
    private String email;
    private UserRole role;
    private String roleDisplayName;
    private Boolean isActive;
}
