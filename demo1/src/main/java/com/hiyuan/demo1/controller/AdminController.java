package com.hiyuan.demo1.controller;

import com.hiyuan.demo1.dto.response.ApiResponse;
import com.hiyuan.demo1.entity.Document;
import com.hiyuan.demo1.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员控制器
 * 提供管理员专用的API接口
 */
@Slf4j
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final DocumentRepository documentRepository;

    /**
     * 获取所有文档列表（管理员权限）
     */
    @GetMapping("/documents")
    public ApiResponse<List<Document>> getAllDocuments() {
        log.info("管理员获取所有文档列表");
        
        try {
            // 查询所有文档，按上传时间倒序
            List<Document> documents = documentRepository.findAll(
                Sort.by(Sort.Direction.DESC, "uploadedAt")
            );
            
            log.info("找到 {} 个文档", documents.size());
            return ApiResponse.success(documents);
        } catch (Exception e) {
            log.error("获取文档列表失败: {}", e.getMessage(), e);
            return ApiResponse.serverError("获取文档列表失败");
        }
    }
}
