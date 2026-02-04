package com.hiyuan.demo1.controller;

import com.hiyuan.demo1.dto.auth.AuthResponse;
import com.hiyuan.demo1.dto.auth.LoginRequest;
import com.hiyuan.demo1.entity.User;
import com.hiyuan.demo1.entity.UserRole;
import com.hiyuan.demo1.repository.UserRepository;
import com.hiyuan.demo1.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AuthController 测试类
 * 
 * 测试 JWT 登录、注册、刷新等功能
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
@DirtiesContext
class AuthControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private User testUser;
    private String testPassword = "123456";

    @BeforeEach
    void setUp() {
        // 清理测试用户
        userRepository.findByUsername("testuser").ifPresent(userRepository::delete);
        
        // 创建测试用户 - id自动生成，不需要手动设置
        testUser = User.builder()
                .username("testuser")
                .password(passwordEncoder.encode(testPassword))
                .email("test@example.com")
                .role(UserRole.STUDENT)
                .isActive(true)
                .build();
        testUser = userRepository.save(testUser);
    }

    @Test
    @DisplayName("测试正常登录 - 学生角色")
    void testLoginSuccess_Student() {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword(testPassword);

        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                "/api/auth/login",
                request,
                AuthResponse.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getAccessToken());
        assertNotNull(response.getBody().getRefreshToken());
        assertEquals("testuser", response.getBody().getUser().getUsername());
        assertEquals(UserRole.STUDENT, response.getBody().getUser().getRole());
    }

    @Test
    @DisplayName("测试正常登录 - 教师角色")
    void testLoginSuccess_Teacher() {
        // 创建教师用户
        User teacher = User.builder()
                .username("teacheruser")
                .password(passwordEncoder.encode(testPassword))
                .email("teacher@example.com")
                .role(UserRole.TEACHER)
                .isActive(true)
                .build();
        userRepository.save(teacher);

        LoginRequest request = new LoginRequest();
        request.setUsername("teacheruser");
        request.setPassword(testPassword);

        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                "/api/auth/login",
                request,
                AuthResponse.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(UserRole.TEACHER, response.getBody().getUser().getRole());
    }

    @Test
    @DisplayName("测试正常登录 - 管理员角色")
    void testLoginSuccess_Admin() {
        // 创建管理员用户
        User admin = User.builder()
                .username("adminuser")
                .password(passwordEncoder.encode(testPassword))
                .email("admin@example.com")
                .role(UserRole.ADMIN)
                .isActive(true)
                .build();
        userRepository.save(admin);

        LoginRequest request = new LoginRequest();
        request.setUsername("adminuser");
        request.setPassword(testPassword);

        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                "/api/auth/login",
                request,
                AuthResponse.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(UserRole.ADMIN, response.getBody().getUser().getRole());
    }

    @Test
    @DisplayName("测试登录失败 - 错误密码")
    void testLoginFailure_WrongPassword() {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("wrongpassword");

        ResponseEntity<Object> response = restTemplate.postForEntity(
                "/api/auth/login",
                request,
                Object.class
        );

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @DisplayName("测试登录失败 - 用户不存在")
    void testLoginFailure_UserNotFound() {
        LoginRequest request = new LoginRequest();
        request.setUsername("nonexistent");
        request.setPassword("123456");

        ResponseEntity<Object> response = restTemplate.postForEntity(
                "/api/auth/login",
                request,
                Object.class
        );

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @DisplayName("测试登录失败 - 禁用用户")
    void testLoginFailure_DisabledUser() {
        testUser.setIsActive(false);
        userRepository.save(testUser);

        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword(testPassword);

        ResponseEntity<Object> response = restTemplate.postForEntity(
                "/api/auth/login",
                request,
                Object.class
        );

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @DisplayName("测试Token刷新")
    void testRefreshToken() {
        // 先生成 refresh token
        String refreshToken = jwtTokenProvider.generateRefreshToken(testUser);

        // 构建刷新请求
        String requestBody = String.format("{\"refreshToken\": \"%s\"}", refreshToken);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                "/api/auth/refresh",
                entity,
                AuthResponse.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getAccessToken());
        // refresh token 应该保持不变
        assertEquals(refreshToken, response.getBody().getRefreshToken());
    }

    @Test
    @DisplayName("测试Token刷新失败 - 无效token")
    void testRefreshToken_InvalidToken() {
        String requestBody = "{\"refreshToken\": \"invalid_token\"}";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Object> response = restTemplate.postForEntity(
                "/api/auth/refresh",
                entity,
                Object.class
        );

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @DisplayName("测试使用Access Token访问受保护接口")
    void testAccessProtectedResource() {
        // 生成 access token
        String accessToken = jwtTokenProvider.generateAccessToken(testUser);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<Object> response = restTemplate.exchange(
                "/api/auth/me",
                HttpMethod.GET,
                entity,
                Object.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("测试使用无效Token访问受保护接口")
    void testAccessProtectedResource_InvalidToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("invalid_token");
        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<Object> response = restTemplate.exchange(
                "/api/auth/me",
                HttpMethod.GET,
                entity,
                Object.class
        );

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @DisplayName("测试使用过期Token访问受保护接口")
    void testAccessProtectedResource_ExpiredToken() {
        // 过期的token应该被拒绝
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("expired_token_example");
        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<Object> response = restTemplate.exchange(
                "/api/auth/me",
                HttpMethod.GET,
                entity,
                Object.class
        );

        // 过期的token应该被拒绝
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @DisplayName("测试登出")
    void testLogout() {
        String accessToken = jwtTokenProvider.generateAccessToken(testUser);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<Object> response = restTemplate.exchange(
                "/api/auth/logout",
                HttpMethod.POST,
                entity,
                Object.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}