package com.hiyuan.demo1.service;

import com.hiyuan.demo1.dto.auth.*;
import com.hiyuan.demo1.entity.User;
import com.hiyuan.demo1.entity.UserRole;
import com.hiyuan.demo1.repository.UserRepository;
import com.hiyuan.demo1.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 认证服务
 * 
 * 处理登录、注册、Token刷新等认证相关逻辑
 * 
 * @author 开发团队
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 用户登录
     * 
     * @param request 登录请求
     * @return 认证响应（包含Token和用户信息）
     */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for user: {}", request.getUsername());

        // #region agent log
        try {
            java.nio.file.Path __logPath = java.nio.file.Paths.get("d:\\desktop\\myProject\\.cursor\\debug.log");
            java.nio.file.Files.createDirectories(__logPath.getParent());
            java.nio.file.Files.writeString(
                    __logPath,
                    "{\"sessionId\":\"debug-session\",\"runId\":\"login-check\",\"hypothesisId\":\"H6\",\"location\":\"AuthService.java:45\",\"message\":\"login called\",\"data\":{\"username\":\""
                            + request.getUsername() + "\"},\"timestamp\":" + System.currentTimeMillis() + "}\n",
                    java.nio.charset.StandardCharsets.UTF_8,
                    java.nio.file.StandardOpenOption.CREATE,
                    java.nio.file.StandardOpenOption.APPEND
            );
        } catch (Exception ignored) {
        }
        // #endregion

        // 1. 认证用户名和密码
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        // 2. 获取用户信息
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("用户不存在"));

        if (!user.getIsActive()) {
            throw new BadCredentialsException("用户已被禁用");
        }

        // #region agent log
        try {
            java.nio.file.Path __logPath = java.nio.file.Paths.get("d:\\desktop\\myProject\\.cursor\\debug.log");
            java.nio.file.Files.createDirectories(__logPath.getParent());
            java.nio.file.Files.writeString(
                    __logPath,
                    "{\"sessionId\":\"debug-session\",\"runId\":\"login-check\",\"hypothesisId\":\"H7\",\"location\":\"AuthService.java:68\",\"message\":\"login success\",\"data\":{\"userId\":\""
                            + user.getId() + "\",\"role\":\"" + user.getRole()
                            + "\"},\"timestamp\":" + System.currentTimeMillis() + "}\n",
                    java.nio.charset.StandardCharsets.UTF_8,
                    java.nio.file.StandardOpenOption.CREATE,
                    java.nio.file.StandardOpenOption.APPEND
            );
        } catch (Exception ignored) {
        }
        // #endregion

        // 3. 生成双Token
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);

        log.info("User logged in successfully: {}", user.getUsername());

        // 4. 构建响应
        return buildAuthResponse(accessToken, refreshToken, user);
    }

    /**
     * 用户注册
     * 
     * @param request 注册请求
     * @return 认证响应
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // #region agent log
        try {
            java.nio.file.Path __logPath = java.nio.file.Paths.get(System.getProperty("user.dir"), ".cursor", "debug.log");
            java.nio.file.Files.createDirectories(__logPath.getParent());
            java.nio.file.Files.writeString(
                    __logPath,
                    "{\"sessionId\":\"debug-session\",\"runId\":\"initial\",\"hypothesisId\":\"H2\",\"location\":\"AuthService.java:82\",\"message\":\"register service called\",\"data\":{\"username\":\""
                            + request.getUsername() + "\",\"email\":\"" + request.getEmail() + "\",\"role\":\""
                            + request.getRole() + "\"},\"timestamp\":" + System.currentTimeMillis() + "}\n",
                    java.nio.charset.StandardCharsets.UTF_8,
                    java.nio.file.StandardOpenOption.CREATE,
                    java.nio.file.StandardOpenOption.APPEND
            );
        } catch (Exception ignored) {
        }
        // #endregion
        log.info("Registration attempt for user: {}", request.getUsername());

        // 1. 检查用户名是否已存在
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadCredentialsException("用户名已存在");
        }

        // 2. 检查邮箱是否已被使用（如果提供了邮箱）
        if (request.getEmail() != null && !request.getEmail().isEmpty() &&
            userRepository.existsByEmail(request.getEmail())) {
            throw new BadCredentialsException("邮箱已被注册");
        }

        // 3. 创建新用户
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .role(request.getRole() != null ? request.getRole() : UserRole.STUDENT)
                .isActive(true)
                .build();

        user = userRepository.save(user);

        // 4. 生成Token
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);

        log.info("User registered successfully: {}", user.getUsername());

        // 5. 构建响应
        return buildAuthResponse(accessToken, refreshToken, user);
    }

    /**
     * 刷新 Access Token
     * 
     * @param refreshToken Refresh Token
     * @return 新的认证响应
     */
    @Transactional(readOnly = true)
    public AuthResponse refreshToken(String refreshToken) {
        log.info("Token refresh attempt");

        // 1. 验证 Refresh Token
        if (!jwtTokenProvider.validateToken(refreshToken) || 
            !jwtTokenProvider.isRefreshToken(refreshToken)) {
            throw new BadCredentialsException("无效的 Refresh Token");
        }

        // 2. 获取用户ID
        UUID userId = jwtTokenProvider.getUserIdFromToken(refreshToken);

        // 3. 验证用户是否存在且激活
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadCredentialsException("用户不存在"));

        if (!user.getIsActive()) {
            throw new BadCredentialsException("用户已被禁用");
        }

        // 4. 生成新的 Access Token
        String newAccessToken = jwtTokenProvider.generateAccessToken(user);

        log.info("Token refreshed successfully for user: {}", user.getUsername());

        // 5. 构建响应（Refresh Token 保持不变）
        return buildAuthResponse(newAccessToken, refreshToken, user);
    }

    /**
     * 获取当前用户信息
     * 
     * @param userId 用户ID
     * @return 用户信息
     */
    @Transactional(readOnly = true)
    public UserInfoResponse getUserInfo(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadCredentialsException("用户不存在"));

        return buildUserInfoResponse(user);
    }

    /**
     * 构建认证响应
     */
    private AuthResponse buildAuthResponse(String accessToken, String refreshToken, User user) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenExpiration() / 1000)
                .user(buildUserInfoResponse(user))
                .build();
    }

    /**
     * 构建用户信息响应
     */
    private UserInfoResponse buildUserInfoResponse(User user) {
        return UserInfoResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .roleDisplayName(user.getRole().getChineseName())
                .isActive(user.getIsActive())
                .build();
    }
}
