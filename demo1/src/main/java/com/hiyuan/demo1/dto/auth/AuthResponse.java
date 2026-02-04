package com.hiyuan.demo1.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 认证响应 DTO
 * 
 * 登录/注册/刷新成功后返回给前端的响应数据
 * 包含双 Token 和用户信息
 * 
 * @author 开发团队
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;
    private UserInfoResponse user;
}
