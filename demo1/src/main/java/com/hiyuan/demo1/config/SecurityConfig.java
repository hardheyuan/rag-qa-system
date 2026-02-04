package com.hiyuan.demo1.config;

import com.hiyuan.demo1.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security 配置类
 * 
 * 配置应用程序的安全规则、认证和授权机制
 * 使用 JWT 进行无状态认证
 * 
 * 安全规则：
 * 1. 公开端点：/api/auth/** (登录、注册、刷新)
 * 2. 角色权限控制：
 *    - /api/admin/**: 仅 ADMIN
 *    - /api/teacher/**: TEACHER 或 ADMIN
 *    - /api/student/**: 所有认证用户
 * 3. 其他端点：需要认证
 * 
 * @author 开发团队
 * @version 1.0.0
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;
    private final AuthenticationEntryPoint authenticationEntryPoint;
    private final AccessDeniedHandler accessDeniedHandler;

    /**
     * 配置安全过滤器链
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 禁用 CSRF（JWT 无状态不需要）
            .csrf(AbstractHttpConfigurer::disable)
            
            // 配置 CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // 配置会话管理为无状态
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // 配置请求授权规则
            .authorizeHttpRequests(auth -> auth
                // 公开端点 - 不需要认证
                // 注意：logout 需要认证，已单独配置
                .requestMatchers("/auth/login", "/auth/register", "/auth/refresh").permitAll()
                .requestMatchers("/public/**").permitAll()
                .requestMatchers("/actuator/health").permitAll()

                // 管理员端点
                .requestMatchers("/admin/**").hasRole("ADMIN")

                // 教师端点（教师和管理员可访问）
                .requestMatchers("/teacher/**").hasAnyRole("TEACHER", "ADMIN")

                // 学生端点（所有认证用户可访问）
                .requestMatchers("/student/**").authenticated()

                // 文档上传端点（仅教师和管理员）
                .requestMatchers(HttpMethod.POST, "/documents").hasAnyRole("TEACHER", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/documents/**").hasAnyRole("TEACHER", "ADMIN")

                // 其他所有端点需要认证
                .anyRequest().authenticated()
            )
            
            // 配置异常处理
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler)
            )
            
            // 添加 JWT 过滤器
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 配置密码编码器（BCrypt）
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 配置认证提供者
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * 配置认证管理器
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * CORS 配置
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173")); // Vite 默认端口
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
