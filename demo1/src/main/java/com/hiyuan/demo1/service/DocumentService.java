package com.hiyuan.demo1.service;

import com.hiyuan.demo1.entity.Document;
import com.hiyuan.demo1.entity.User;
import com.hiyuan.demo1.enums.DocumentStatus;
import com.hiyuan.demo1.enums.FileType;
import com.hiyuan.demo1.exception.DocumentNotFoundException;
import com.hiyuan.demo1.exception.AuthorizationException;
import com.hiyuan.demo1.exception.BusinessException;
import com.hiyuan.demo1.repository.DocumentRepository;
import com.hiyuan.demo1.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 文档服务 - 处理文档上传
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final DocumentProcessorService processorService;

    @Value("${document.upload-dir:./uploads}")
    private String uploadDir;

    /**
     * 上传并处理文档
     */
    @Transactional
    public Document uploadDocument(MultipartFile file, UUID userId) throws IOException {
        log.info("开始上传文档: filename={}, userId={}", file.getOriginalFilename(), userId);

        // 1. 验证文件
        String filename = file.getOriginalFilename();
        if (filename == null || filename.isEmpty()) {
            throw BusinessException.badRequest("filename", "文件名不能为空");
        }

        if (!FileType.isSupported(filename)) {
            throw BusinessException.badRequest("fileType",
                "不支持的文件格式，请上传 PDF、DOCX 或 PPTX 文件");
        }

        // 2. 获取当前登录用户
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthorizationException("用户不存在或登录状态无效"));

        // 3. 检查是否已存在同名文件
        if (documentRepository.findByUserIdAndFilename(user.getId(), filename).isPresent()) {
            throw BusinessException.badRequest("filename",
                "已存在同名文件: " + filename);
        }

        // 4. 保存文件到磁盘
        String filePath = saveFileToDisk(file, user.getId().toString());

        // 5. 创建文档记录
        FileType fileType = FileType.fromExtension(getFileExtension(filename));
        Document document = Document.builder()
                .user(user)
                .filename(filename)
                .filePath(filePath)
                .fileSize(file.getSize())
                .fileType(fileType)
                .status(DocumentStatus.UPLOADING)
                .uploadedAt(LocalDateTime.now())
                .build();

        document = documentRepository.save(document);
        log.info("文档记录已创建: id={}", document.getId());

        UUID documentId = document.getId();
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    processorService.processDocumentAsync(documentId);
                }
            });
        } else {
            processorService.processDocumentAsync(documentId);
        }

        return document;
    }

    /**
     * 保存文件到磁盘
     */
    private String saveFileToDisk(MultipartFile file, String userId) throws IOException {
        Path userDir = Paths.get(uploadDir, userId);
        Files.createDirectories(userDir);

        String originalFilename = file.getOriginalFilename();
        String filename = UUID.randomUUID() + "_" + originalFilename;
        Path filePath = userDir.resolve(filename);

        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        log.info("文件已保存: {}", filePath);

        return filePath.toString();
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot > 0) {
            return filename.substring(lastDot + 1);
        }
        return "";
    }

    /**
     * 删除文档及其所有关联数据
     */
    @Transactional
    public void deleteDocument(UUID documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> DocumentNotFoundException.forId(documentId));

        try {
            Path filePath = Paths.get(document.getFilePath());
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.warn("删除文件失败: {}", e.getMessage());
        }

        documentRepository.delete(document);
        log.info("文档已删除: id={}", documentId);
    }

    /**
     * 获取文档检索统计（包含检索次数和覆盖率）
     */
    public List<java.util.Map<String, Object>> getDocumentStats(UUID userId) {
        // 1. 获取用户文档
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return List.of();
        }

        List<Document> documents = documentRepository.findByUserId(user.getId());
        if (documents.isEmpty()) {
            return List.of();
        }

        // 2. 获取所有问答历史中的检索文档统计
        // 从 QaHistory 中统计每个文档被检索的次数
        List<java.util.Map<String, Object>> stats = new java.util.ArrayList<>();
        
        for (Document doc : documents) {
            java.util.Map<String, Object> stat = new java.util.HashMap<>();
            stat.put("id", doc.getId().toString());
            stat.put("name", doc.getFilename());
            stat.put("type", doc.getFileType() != null ? doc.getFileType().toString() : "UNKNOWN");
            stat.put("size", doc.getFileSize());
            stat.put("status", doc.getStatus().toString());
            stat.put("createTime", doc.getUploadedAt());
            
            // 统计检索次数（目前用 mock 数据，后续可以从 QaHistory 统计）
            // 这里先用基于文档分块数的模拟数据
            int chunkCount = doc.getChunkCount() != null ? doc.getChunkCount() : 0;
            int retrievalCount = Math.max(chunkCount * 3, (int) (Math.random() * 50) + 10); // 模拟检索次数
            stat.put("retrievals", retrievalCount);
            
            // 计算覆盖率：基于分块处理状态
            // 如果有分块且状态为 SUCCESS，则认为覆盖率高
            int coverage;
            if (doc.getStatus() == DocumentStatus.SUCCESS && chunkCount > 0) {
                coverage = 85 + (int) (Math.random() * 15); // 85-100%
            } else if (doc.getStatus() == DocumentStatus.SUCCESS) {
                coverage = 70 + (int) (Math.random() * 15); // 70-85%
            } else if (doc.getStatus() == DocumentStatus.PROCESSING) {
                coverage = 40 + (int) (Math.random() * 30); // 40-70%
            } else {
                coverage = (int) (Math.random() * 40); // 0-40%
            }
            stat.put("coverage", coverage);
            
            stats.add(stat);
        }

        // 按检索次数排序
        stats.sort((a, b) -> ((Integer) b.get("retrievals")).compareTo((Integer) a.get("retrievals")));
        
        return stats;
    }
}
