package com.hiyuan.demo1.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

/**
 * JWT 认证过滤器
 * 
 * 拦截所有请求，从请求头中提取 JWT Token 并验证
 * 如果 Token 有效，将用户信息设置到 Spring Security 上下文中
 * 
 * 处理流程：
 * 1. 从请求头中提取 Authorization
 * 2. 解析并验证 JWT Token
 * 3. 创建 UserPrincipal 并设置到 SecurityContext
 * 4. 放行请求到后续过滤器
 * 
 * @author 开发团队
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // 1. 从请求中获取 JWT Token
            String jwt = getJwtFromRequest(request);

            // #region agent log
            try {
                java.nio.file.Path __logPath = java.nio.file.Paths.get("d:\\desktop\\myProject\\.cursor\\debug.log");
                java.nio.file.Files.createDirectories(__logPath.getParent());
                java.nio.file.Files.writeString(
                        __logPath,
                        "{\"sessionId\":\"debug-session\",\"runId\":\"initial\",\"hypothesisId\":\"H5\",\"location\":\"JwtAuthenticationFilter.java:52\",\"message\":\"JWT filter invoked\",\"data\":{\"uri\":\""
                                + request.getRequestURI() + "\",\"hasAuthorizationHeader\":"
                                + (request.getHeader("Authorization") != null) + ",\"hasToken\":"
                                + (jwt != null) + "},\"timestamp\":" + System.currentTimeMillis() + "}\n",
                        java.nio.charset.StandardCharsets.UTF_8,
                        java.nio.file.StandardOpenOption.CREATE,
                        java.nio.file.StandardOpenOption.APPEND
                );
            } catch (Exception ignored) {
            }
            // #endregion

            // 2. 验证 Token 并设置认证信息
            if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {
                // 3. 从 Token 中提取用户信息
                UUID userId = jwtTokenProvider.getUserIdFromToken(jwt);
                String username = jwtTokenProvider.getUsernameFromToken(jwt);
                var role = jwtTokenProvider.getRoleFromToken(jwt);

                // 4. 创建 UserPrincipal
                UserPrincipal userPrincipal = new UserPrincipal(userId, username, null, role);

                // 5. 创建认证 Token
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userPrincipal,
                                null,
                                userPrincipal.getAuthorities()
                        );

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 6. 设置到 SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("Authenticated user: {}, URI: {}", username, request.getRequestURI());
            }
        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }

        // 继续过滤器链
        filterChain.doFilter(request, response);
    }

    /**
     * 从请求头中提取 JWT Token
     * 
     * 支持的格式：
     * Authorization: Bearer <token>
     * 
     * @param request HTTP 请求
     * @return JWT Token 或 null
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        String tokenParam = request.getParameter("access_token");
        if (StringUtils.hasText(tokenParam)) {
            return tokenParam;
        }
        
        return null;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // 公开路径不需要 JWT 验证
        String path = request.getRequestURI();
        String contextPath = request.getContextPath();
        
        // 移除 context-path（如果有）
        if (path.startsWith(contextPath) && !contextPath.isEmpty()) {
            path = path.substring(contextPath.length());
        }
        
        // 如果路径以 /api 开头（测试环境contextPath为空时），也去掉它
        if (path.startsWith("/api")) {
            path = path.substring(4); // 去掉 "/api"
        }
        
        log.debug("shouldNotFilter check - normalized path: {}, original: {}, contextPath: '{}'", 
                  path, request.getRequestURI(), contextPath);

        // 仅放行登录和刷新，注册需要管理员权限
        if (path.equals("/auth/login") ||
            path.equals("/auth/refresh")) {
            log.debug(" shouldNotFilter = true (auth endpoint)");
            return true;
        }

        boolean shouldSkip = path.startsWith("/public/") ||
               path.equals("/actuator/health");
        log.debug(" shouldNotFilter = {}", shouldSkip);
        return shouldSkip;
    }
}
