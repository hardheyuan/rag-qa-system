package com.hiyuan.demo1.controller;

import com.hiyuan.demo1.dto.response.ApiResponse;
import com.hiyuan.demo1.repository.DocumentRepository;
import com.hiyuan.demo1.repository.QaHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 统计信息控制器
 */
@Slf4j
@RestController
@RequestMapping("/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final DocumentRepository documentRepository;
    private final QaHistoryRepository qaHistoryRepository;

    /**
     * 获取系统统计信息
     */
    @GetMapping
    public ApiResponse<Map<String, Object>> getStatistics() {
        log.info("获取系统统计信息");
        
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // 文档统计
            long totalDocuments = documentRepository.count();
            stats.put("totalDocuments", totalDocuments);
            
            // 问答统计
            long totalQuestions = qaHistoryRepository.count();
            stats.put("totalQuestions", totalQuestions);
            
            // 可以添加更多统计信息
            stats.put("timestamp", java.time.LocalDateTime.now());
            
            return ApiResponse.success(stats);
        } catch (Exception e) {
            log.error("获取统计信息失败: {}", e.getMessage(), e);
            return ApiResponse.serverError("获取统计信息失败");
        }
    }
}
