package com.hiyuan.demo1.controller;

import com.hiyuan.demo1.dto.auth.*;
import com.hiyuan.demo1.security.UserPrincipal;
import com.hiyuan.demo1.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 * 
 * 处理用户认证相关请求：
 * - 登录
 * - 注册
 * - 刷新Token
 * - 登出
 * - 获取当前用户信息
 * 
 * 端点路径：/api/auth/**
 * 
 * @author 开发团队
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录
     * 
     * POST /api/auth/login
     * 
     * @param request 登录请求
     * @return 认证响应（包含双Token和用户信息）
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest request) {
        log.info("Login request received for user: {}", request.getUsername());
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 用户注册
     * 
     * POST /api/auth/register
     * 
     * @param request 注册请求
     * @return 认证响应
     */
    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AuthResponse> register(@RequestBody @Valid RegisterRequest request) {
        log.info("Registration request received for user: {}", request.getUsername());
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 刷新 Access Token
     * 
     * POST /api/auth/refresh
     * 
     * @param request 刷新Token请求
     * @return 新的认证响应
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody @Valid RefreshTokenRequest request) {
        log.info("Token refresh request received");
        AuthResponse response = authService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }

    /**
     * 用户登出
     * 
     * POST /api/auth/logout
     * 
     * 注意：JWT 无状态认证，服务器端只需返回成功
     * Token 的失效由前端清除存储实现
     * 生产环境可添加 Token 黑名单机制
     * 
     * @return 成功响应
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        log.info("Logout request received");
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "登出成功");
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取当前登录用户信息
     * 
     * GET /api/auth/me
     * 
     * @param userPrincipal 当前用户主体
     * @return 用户信息
     */
    @GetMapping("/me")
    public ResponseEntity<UserInfoResponse> getCurrentUser(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("Get current user info request received");
        
        if (userPrincipal == null) {
            return ResponseEntity.status(401).build();
        }
        
        UserInfoResponse userInfo = authService.getUserInfo(userPrincipal.getId());
        return ResponseEntity.ok(userInfo);
    }

    /**
     * 验证Token有效性
     * 
     * GET /api/auth/validate
     * 
     * 用于前端检查当前Token是否有效
     * 
     * @return 验证结果
     */
    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        Map<String, Object> response = new HashMap<>();
        
        if (userPrincipal != null) {
            response.put("valid", true);
            response.put("userId", userPrincipal.getId());
            response.put("username", userPrincipal.getUsername());
            response.put("role", userPrincipal.getRole());
        } else {
            response.put("valid", false);
        }
        
        return ResponseEntity.ok(response);
    }
}
