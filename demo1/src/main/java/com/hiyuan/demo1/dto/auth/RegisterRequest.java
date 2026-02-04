package com.hiyuan.demo1.dto.auth;

import com.hiyuan.demo1.entity.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 注册请求 DTO
 * 
 * 用户注册时提交的请求数据
 * 
 * @author 开发团队
 * @version 1.0.0
 */
@Data
public class RegisterRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 100, message = "用户名长度必须在3-100字符之间")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, message = "密码至少6位")
    private String password;

    @Email(message = "邮箱格式不正确")
    private String email;

    @NotNull(message = "角色不能为空")
    private UserRole role;
}
