package com.hiyuan.demo1.controller;

import com.hiyuan.demo1.dto.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 系统状态控制器
 */
@Slf4j
@RestController
@RequestMapping("/system")
@RequiredArgsConstructor
public class SystemController {

    private final DataSource dataSource;

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public ApiResponse<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now());
        return ApiResponse.success(health);
    }

    /**
     * 系统状态
     */
    @GetMapping("/status")
    public ApiResponse<Map<String, Object>> status() {
        Map<String, Object> status = new HashMap<>();
        
        // 检查数据库连接
        String dbStatus = "DISCONNECTED";
        try (Connection conn = dataSource.getConnection()) {
            if (conn.isValid(5)) {
                dbStatus = "CONNECTED";
            }
        } catch (Exception e) {
            log.warn("数据库连接检查失败: {}", e.getMessage());
        }
        
        status.put("database", dbStatus);
        status.put("timestamp", LocalDateTime.now());
        status.put("application", "RAG QA System");
        
        return ApiResponse.success(status);
    }
}
