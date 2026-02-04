package com.hiyuan.demo1.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 刷新 Token 请求 DTO
 * 
 * 使用 Refresh Token 换取新的 Access Token
 * 
 * @author 开发团队
 * @version 1.0.0
 */
@Data
public class RefreshTokenRequest {

    @NotBlank(message = "Refresh Token 不能为空")
    private String refreshToken;
}
