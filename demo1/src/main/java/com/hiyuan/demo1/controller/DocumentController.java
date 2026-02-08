package com.hiyuan.demo1.controller;

import com.hiyuan.demo1.dto.response.ApiResponse;
import com.hiyuan.demo1.entity.Document;
import com.hiyuan.demo1.enums.DocumentStatus;
import com.hiyuan.demo1.exception.BusinessException;
import com.hiyuan.demo1.repository.DocumentRepository;
import com.hiyuan.demo1.security.UserPrincipal;
import com.hiyuan.demo1.service.DocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 文档管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentRepository documentRepository;
    private final DocumentService documentService;

    /**
     * 上传文档
     */
    @PostMapping("/upload")
    public ApiResponse<Document> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        if (userPrincipal == null) {
            return ApiResponse.unauthorized("未登录或登录状态失效");
        }

        UUID userId = userPrincipal.getId();
        log.info("上传文档: filename={}, size={}, userId={}", 
                file.getOriginalFilename(), file.getSize(), userId);
        
        try {
            Document document = documentService.uploadDocument(file, userId);
            return ApiResponse.success("文件上传成功，正在处理中", document);
        } catch (BusinessException e) {
            log.warn("上传业务校验失败: {}", e.getMessage());
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("上传参数错误: {}", e.getMessage());
            return ApiResponse.badRequest(e.getMessage());
        } catch (Exception e) {
            log.error("上传文档失败: {}", e.getMessage(), e);
            return ApiResponse.serverError("服务器内部错误，请稍后重试");
        }
    }

    /**
     * 获取文档列表
     */
    @GetMapping
    public ApiResponse<List<Document>> listDocuments(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        if (userPrincipal == null) {
            return ApiResponse.unauthorized("未登录或登录状态失效");
        }

        UUID userId = userPrincipal.getId();
        log.info("获取文档列表: userId={}, page={}, size={}", userId, page, size);

        try {
            // 查询用户的文档，按上传时间倒序
            var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "uploadedAt"));
            Page<Document> documents = documentRepository.findByUserId(userId, pageable);
            return ApiResponse.success(documents.getContent());
        } catch (Exception e) {
            log.error("获取文档列表失败: {}", e.getMessage(), e);
            return ApiResponse.serverError("获取文档列表失败");
        }
    }

    /**
     * 获取文档详情
     */
    @GetMapping("/{id}")
    public ApiResponse<Document> getDocument(@PathVariable String id) {
        log.info("获取文档详情: id={}", id);
        
        try {
            UUID uuid = UUID.fromString(id);
            Document document = documentRepository.findById(uuid)
                    .orElseThrow(() -> new RuntimeException("文档不存在"));
            return ApiResponse.success(document);
        } catch (Exception e) {
            log.error("获取文档详情失败: {}", e.getMessage(), e);
            return ApiResponse.serverError(e.getMessage());
        }
    }

    /**
     * 删除文档
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteDocument(@PathVariable String id) {
        log.info("删除文档: id={}", id);
        
        try {
            UUID uuid = UUID.fromString(id);
            documentService.deleteDocument(uuid);
            return ApiResponse.success("文档删除成功", null);
        } catch (Exception e) {
            log.error("删除文档失败: {}", e.getMessage(), e);
            return ApiResponse.serverError("删除文档失败");
        }
    }

    /**
     * 清理卡住的文档（将 UPLOADING 状态超过 5 分钟的文档标记为失败）
     */
    @PostMapping("/cleanup")
    public ApiResponse<Integer> cleanupStuckDocuments() {
        log.info("开始清理卡住的文档");
        
        try {
            List<Document> stuckDocs = documentRepository.findDocumentsToProcess(DocumentStatus.UPLOADING);
            int count = 0;
            LocalDateTime threshold = LocalDateTime.now().minusMinutes(5);
            
            for (Document doc : stuckDocs) {
                if (doc.getUploadedAt().isBefore(threshold)) {
                    doc.setStatus(DocumentStatus.FAILED);
                    doc.setErrorMessage("处理超时，请重新上传");
                    doc.setProcessedAt(LocalDateTime.now());
                    documentRepository.save(doc);
                    count++;
                    log.info("已清理文档: id={}, filename={}", doc.getId(), doc.getFilename());
                }
            }
            
            return ApiResponse.success("已清理 " + count + " 个卡住的文档", count);
        } catch (Exception e) {
            log.error("清理文档失败: {}", e.getMessage(), e);
            return ApiResponse.serverError("清理失败");
        }
    }

    /**
     * 获取文档检索统计（包含检索次数和覆盖率）
     */
    @GetMapping("/stats")
    public ApiResponse<List<java.util.Map<String, Object>>> getDocumentStats(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        if (userPrincipal == null) {
            return ApiResponse.unauthorized("未登录或登录状态失效");
        }

        UUID userId = userPrincipal.getId();
        log.info("获取文档检索统计: userId={}", userId);
        
        try {
            List<java.util.Map<String, Object>> stats = documentService.getDocumentStats(userId);
            return ApiResponse.success("获取文档统计成功", stats);
        } catch (Exception e) {
            log.error("获取文档统计失败: {}", e.getMessage(), e);
            return ApiResponse.serverError("获取文档统计失败: " + e.getMessage());
        }
    }
}
