package com.hiyuan.demo1.controller;

import com.hiyuan.demo1.dto.response.ApiResponse;
import com.hiyuan.demo1.entity.QaHistory;
import com.hiyuan.demo1.exception.AuthorizationException;
import com.hiyuan.demo1.repository.QaHistoryRepository;
import com.hiyuan.demo1.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

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
     * 获取当前用户的问答历史列表
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Page<QaHistory>> listHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        log.info("用户 {} 获取问答历史列表: page={}, size={}", userPrincipal.getUsername(), page, size);

        try {
            PageRequest pageRequest = PageRequest.of(page, size,
                    Sort.by(Sort.Direction.DESC, "askedAt"));
            Page<QaHistory> history = qaHistoryRepository.findByUserId(userPrincipal.getId(), pageRequest);
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
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<QaHistory> getHistory(
            @PathVariable String id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("用户 {} 获取问答历史详情: id={}", userPrincipal.getUsername(), id);

        try {
            UUID uuid = UUID.fromString(id);
            QaHistory history = qaHistoryRepository.findById(uuid)
                    .orElseThrow(() -> new RuntimeException("问答历史不存在"));

            if (history.getUser() != null && !history.getUser().getId().equals(userPrincipal.getId())
                    && !userPrincipal.hasRole(com.hiyuan.demo1.entity.UserRole.ADMIN)) {
                log.warn("用户 {} 无权访问问答历史 {}", userPrincipal.getUsername(), id);
                throw new AuthorizationException("无权限访问该问答历史");
            }

            return ApiResponse.success(history);
        } catch (IllegalArgumentException e) {
            log.error("无效的UUID格式: {}", id);
            return ApiResponse.badRequest("无效的ID格式");
        } catch (AuthorizationException e) {
            log.error("权限错误: {}", e.getMessage());
            return ApiResponse.forbidden(e.getMessage());
        } catch (Exception e) {
            log.error("获取问答历史失败: {}", e.getMessage(), e);
            return ApiResponse.serverError(e.getMessage());
        }
    }

    /**
     * 删除问答历史
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> deleteHistory(
            @PathVariable String id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("用户 {} 删除问答历史: id={}", userPrincipal.getUsername(), id);

        try {
            UUID uuid = UUID.fromString(id);
            QaHistory history = qaHistoryRepository.findById(uuid)
                    .orElseThrow(() -> new RuntimeException("问答历史不存在"));

            if (history.getUser() != null && !history.getUser().getId().equals(userPrincipal.getId())
                    && !userPrincipal.hasRole(com.hiyuan.demo1.entity.UserRole.ADMIN)) {
                log.warn("用户 {} 无权删除问答历史 {}", userPrincipal.getUsername(), id);
                throw new AuthorizationException("无权限删除该问答历史");
            }

            qaHistoryRepository.deleteById(uuid);
            return ApiResponse.success("问答历史删除成功", null);
        } catch (IllegalArgumentException e) {
            log.error("无效的UUID格式: {}", id);
            return ApiResponse.badRequest("无效的ID格式");
        } catch (AuthorizationException e) {
            log.error("权限错误: {}", e.getMessage());
            return ApiResponse.forbidden(e.getMessage());
        } catch (Exception e) {
            log.error("删除问答历史失败: {}", e.getMessage(), e);
            return ApiResponse.serverError("删除问答历史失败");
        }
    }

    /**
     * 清空当前用户的所有问答历史
     */
    @DeleteMapping("/clear")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> clearHistory(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("用户 {} 清空自己的问答历史", userPrincipal.getUsername());

        try {
            qaHistoryRepository.deleteByUserId(userPrincipal.getId());
            return ApiResponse.success("历史的问答历史已清空", null);
        } catch (Exception e) {
            log.error("清空问答历史失败: {}", e.getMessage(), e);
            return ApiResponse.serverError("清空问答历史失败");
        }
    }

    /**
     * 管理员：清空所有用户的问答历史
     */
    @DeleteMapping("/clear-all")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> clearAllHistory() {
        log.info("管理员清空所有问答历史");

        try {
            qaHistoryRepository.deleteAll();
            return ApiResponse.success("所有问答历史已清空", null);
        } catch (Exception e) {
            log.error("清空问答历史失败: {}", e.getMessage(), e);
            return ApiResponse.serverError("清空问答历史失败");
        }
    }
}
