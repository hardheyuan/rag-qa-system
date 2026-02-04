package com.hiyuan.demo1.controller;

import com.hiyuan.demo1.dto.response.ApiResponse;
import com.hiyuan.demo1.entity.QaHistory;
import com.hiyuan.demo1.repository.QaHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

/**
 * 问答历史控制器
 */
@Slf4j
@RestController
@RequestMapping("/history")
@RequiredArgsConstructor
public class HistoryController {

    private final QaHistoryRepository qaHistoryRepository;

    /**
     * 获取问答历史列表
     */
    @GetMapping
    public ApiResponse<Page<QaHistory>> listHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("获取问答历史列表: page={}, size={}", page, size);
        
        try {
            // 按提问时间倒序排列
            PageRequest pageRequest = PageRequest.of(page, size, 
                    Sort.by(Sort.Direction.DESC, "askedAt"));
            Page<QaHistory> history = qaHistoryRepository.findAll(pageRequest);
            return ApiResponse.success(history);
        } catch (Exception e) {
            log.error("获取问答历史失败: {}", e.getMessage(), e);
            return ApiResponse.serverError("获取问答历史失败");
        }
    }

    /**
     * 获取单条问答历史
     */
    @GetMapping("/{id}")
    public ApiResponse<QaHistory> getHistory(@PathVariable String id) {
        log.info("获取问答历史详情: id={}", id);
        
        try {
            java.util.UUID uuid = java.util.UUID.fromString(id);
            QaHistory history = qaHistoryRepository.findById(uuid)
                    .orElseThrow(() -> new RuntimeException("问答历史不存在"));
            return ApiResponse.success(history);
        } catch (Exception e) {
            log.error("获取问答历史失败: {}", e.getMessage(), e);
            return ApiResponse.serverError(e.getMessage());
        }
    }

    /**
     * 删除问答历史
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteHistory(@PathVariable String id) {
        log.info("删除问答历史: id={}", id);
        
        try {
            java.util.UUID uuid = java.util.UUID.fromString(id);
            qaHistoryRepository.deleteById(uuid);
            return ApiResponse.success(null);
        } catch (Exception e) {
            log.error("删除问答历史失败: {}", e.getMessage(), e);
            return ApiResponse.serverError("删除问答历史失败");
        }
    }

    /**
     * 清空所有问答历史
     */
    @DeleteMapping("/clear")
    public ApiResponse<Void> clearHistory() {
        log.info("清空所有问答历史");
        
        try {
            qaHistoryRepository.deleteAll();
            return ApiResponse.success("历史记录已清空", null);
        } catch (Exception e) {
            log.error("清空问答历史失败: {}", e.getMessage(), e);
            return ApiResponse.serverError("清空问答历史失败");
        }
    }
}
