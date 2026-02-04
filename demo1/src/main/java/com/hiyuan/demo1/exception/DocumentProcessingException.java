package com.hiyuan.demo1.exception;

import java.util.UUID;

/**
 * 文档处理异常
 *
 * <p>当文档解析、分块或向量化过程中发生错误时抛出此异常</p>
 */
public class DocumentProcessingException extends BusinessException {

    private static final int HTTP_STATUS = 422; // Unprocessable Entity
    private static final String DEFAULT_MESSAGE = "文档处理失败";

    /**
     * 文档ID（可选，用于关联到具体文档）
     */
    private final UUID documentId;

    /**
     * 处理阶段（解析、分块、向量化等）
     */
    private final String processingStage;

    /**
     * 使用默认消息的构造函数
     */
    public DocumentProcessingException() {
        super(HTTP_STATUS, DEFAULT_MESSAGE);
        this.documentId = null;
        this.processingStage = null;
    }

    /**
     * 带自定义消息的构造函数
     */
    public DocumentProcessingException(String message) {
        super(HTTP_STATUS, message);
        this.documentId = null;
        this.processingStage = null;
    }

    /**
     * 关联文档ID的构造函数
     */
    public DocumentProcessingException(UUID documentId, String message) {
        super(HTTP_STATUS,
            String.format("文档处理失败 [ID: %s]: %s", documentId, message));
        this.documentId = documentId;
        this.processingStage = null;
    }

    /**
     * 带处理阶段的构造函数
     */
    public DocumentProcessingException(UUID documentId, String stage, String message) {
        super(HTTP_STATUS,
            String.format("文档处理失败 [ID: %s, 阶段: %s]: %s", documentId, stage, message));
        this.documentId = documentId;
        this.processingStage = stage;
    }

    /**
     * 带原因的构造函数
     */
    public DocumentProcessingException(String message, Throwable cause) {
        super(HTTP_STATUS, message, cause);
        this.documentId = null;
        this.processingStage = null;
    }

    /**
     * 完整参数的构造函数
     */
    public DocumentProcessingException(UUID documentId, String stage,
                                       String message, Throwable cause) {
        super(HTTP_STATUS,
            String.format("文档处理失败 [ID: %s, 阶段: %s]: %s", documentId, stage, message),
            cause);
        this.documentId = documentId;
        this.processingStage = stage;
    }

    // ==================== Getter 方法 ====================

    public UUID getDocumentId() {
        return documentId;
    }

    public String getProcessingStage() {
        return processingStage;
    }

    // ==================== 静态工厂方法 ====================

    /**
     * 创建解析异常
     */
    public static DocumentProcessingException parseError(UUID documentId,
                                                          String fileType, Throwable cause) {
        return new DocumentProcessingException(documentId, "解析",
            String.format("无法解析 %s 文件", fileType), cause);
    }

    /**
     * 创建分块异常
     */
    public static DocumentProcessingException chunkError(UUID documentId, Throwable cause) {
        return new DocumentProcessingException(documentId, "分块",
            "文本分块失败", cause);
    }

    /**
     * 创建向量化异常
     */
    public static DocumentProcessingException embeddingError(UUID documentId,
                                                            String details) {
        return new DocumentProcessingException(documentId, "向量化",
            String.format("生成向量失败: %s", details));
    }

    /**
     * 创建空内容异常
     */
    public static DocumentProcessingException emptyContent(UUID documentId) {
        return new DocumentProcessingException(documentId, "内容验证",
            "文档内容为空或无法提取文本");
    }
}
