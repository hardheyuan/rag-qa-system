package com.hiyuan.demo1.security;

import com.hiyuan.demo1.entity.User;
import com.hiyuan.demo1.entity.UserRole;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * JWT Token 提供者
 * 
 * 负责 JWT Token 的生成、解析和验证
 * 实现双Token策略：Access Token + Refresh Token
 * 
 * Access Token: 短期有效，用于API请求认证
 * Refresh Token: 长期有效，用于刷新Access Token
 * 
 * @author 开发团队
 * @version 1.0.0
 */
@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.access-token.expiration:1800000}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token.expiration:604800000}")
    private long refreshTokenExpiration;

    private SecretKey key;

    /**
     * 初始化密钥
     */
    @PostConstruct
    public void init() {
        // 解码Base64编码的密钥
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        log.info("JWT Token Provider initialized with access token expiration: {}ms, refresh token expiration: {}ms",
                accessTokenExpiration, refreshTokenExpiration);
    }

    /**
     * 生成 Access Token
     * 
     * @param user 用户信息
     * @return JWT Token 字符串
     */
    public String generateAccessToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId().toString());
        claims.put("username", user.getUsername());
        // 处理可能为null的role，默认为STUDENT
        claims.put("role", user.getRole() != null ? user.getRole().name() : "STUDENT");
        claims.put("type", "ACCESS");

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpiration);

        return Jwts.builder()
                .claims(claims)
                .subject(user.getId().toString())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key, Jwts.SIG.HS512)
                .compact();
    }

    /**
     * 生成 Refresh Token
     * 
     * @param user 用户信息
     * @return JWT Token 字符串
     */
    public String generateRefreshToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId().toString());
        claims.put("type", "REFRESH");

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpiration);

        return Jwts.builder()
                .claims(claims)
                .subject(user.getId().toString())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key, Jwts.SIG.HS512)
                .compact();
    }

    /**
     * 从 Token 获取用户ID
     * 
     * @param token JWT Token
     * @return 用户ID
     */
    public UUID getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        String userId = claims.get("userId", String.class);
        return UUID.fromString(userId);
    }

    /**
     * 从 Token 获取用户名
     * 
     * @param token JWT Token
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("username", String.class);
    }

    /**
     * 从 Token 获取用户角色
     * 
     * @param token JWT Token
     * @return 用户角色
     */
    public UserRole getRoleFromToken(String token) {
        Claims claims = parseToken(token);
        String role = claims.get("role", String.class);
        // 如果role为null，返回默认STUDENT
        return role != null ? UserRole.fromString(role) : UserRole.STUDENT;
    }

    /**
     * 验证 Token 是否有效
     * 
     * @param token JWT Token
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT token is expired: {}", e.getMessage());
            return false;
        } catch (UnsupportedJwtException e) {
            log.warn("JWT token is unsupported: {}", e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            log.warn("JWT token is malformed: {}", e.getMessage());
            return false;
        } catch (SignatureException e) {
            log.warn("JWT signature validation failed: {}", e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            log.warn("JWT token is empty or null: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 检查 Token 是否为 Refresh Token
     * 
     * @param token JWT Token
     * @return 是否为 Refresh Token
     */
    public boolean isRefreshToken(String token) {
        try {
            Claims claims = parseToken(token);
            String type = claims.get("type", String.class);
            return "REFRESH".equals(type);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取 Token 过期时间
     * 
     * @param token JWT Token
     * @return 过期时间（毫秒时间戳）
     */
    public long getExpirationDate(String token) {
        Claims claims = parseToken(token);
        return claims.getExpiration().getTime();
    }

    /**
     * 解析 Token
     * 
     * @param token JWT Token
     * @return Claims 对象
     */
    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 获取 Access Token 有效期（毫秒）
     */
    public long getAccessTokenExpiration() {
        return accessTokenExpiration;
    }

    /**
     * 获取 Refresh Token 有效期（毫秒）
     */
    public long getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }
}
