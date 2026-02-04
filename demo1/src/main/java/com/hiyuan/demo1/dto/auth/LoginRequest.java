package com.hiyuan.demo1.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 登录请求 DTO
 * 
 * 用户登录时提交的请求数据
 * 
 * @author 开发团队
 * @version 1.0.0
 */
@Data
public class LoginRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 100, message = "用户名长度必须在3-100字符之间")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, message = "密码至少6位")
    private String password;
}
