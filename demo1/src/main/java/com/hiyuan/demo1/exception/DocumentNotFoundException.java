package com.hiyuan.demo1.exception;

import java.util.UUID;

/**
 * 文档不存在异常
 *
 * <p>当根据ID或其他条件查询文档但找不到时抛出此异常</p>
 */
public class DocumentNotFoundException extends BusinessException {

    private static final int HTTP_STATUS = 404;
    private static final String DEFAULT_MESSAGE = "文档不存在";

    /**
     * 使用默认消息的构造函数
     */
    public DocumentNotFoundException() {
        super(HTTP_STATUS, DEFAULT_MESSAGE);
    }

    /**
     * 带自定义消息的构造函数
     */
    public DocumentNotFoundException(String message) {
        super(HTTP_STATUS, message);
    }

    /**
     * 根据文档ID创建异常
     */
    public DocumentNotFoundException(UUID documentId) {
        super(HTTP_STATUS, String.format("文档不存在，ID: %s", documentId));
    }

    /**
     * 根据文档ID和额外信息的构造函数
     */
    public DocumentNotFoundException(UUID documentId, String additionalInfo) {
        super(HTTP_STATUS,
            String.format("文档不存在，ID: %s，详细信息: %s", documentId, additionalInfo));
    }

    /**
     * 带原因的构造函数
     */
    public DocumentNotFoundException(String message, Throwable cause) {
        super(HTTP_STATUS, message, cause);
    }

    /**
     * 完整参数的构造函数
     */
    public DocumentNotFoundException(UUID documentId, Throwable cause) {
        super(HTTP_STATUS,
            String.format("文档不存在，ID: %s", documentId),
            cause != null ? cause.getMessage() : null,
            cause);
    }

    // ==================== 静态工厂方法 ====================

    /**
     * 创建文档不存在异常
     */
    public static DocumentNotFoundException forId(UUID documentId) {
        return new DocumentNotFoundException(documentId);
    }

    /**
     * 创建文档不存在异常（带文件名）
     */
    public static DocumentNotFoundException forFilename(String filename) {
        return new DocumentNotFoundException(
            String.format("文档不存在: %s", filename));
    }

    /**
     * 创建文档不存在异常（带用户ID和文档ID）
     */
    public static DocumentNotFoundException forUser(String userId, UUID documentId) {
        return new DocumentNotFoundException(
            String.format("用户 %s 的文档不存在，ID: %s", userId, documentId));
    }
}
