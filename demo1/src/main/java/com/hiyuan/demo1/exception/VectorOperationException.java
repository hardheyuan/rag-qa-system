package com.hiyuan.demo1.exception;

import java.util.UUID;

/**
 * 向量操作异常
 *
 * <p>当向量存储、检索或相似度计算过程中发生错误时抛出此异常</p>
 */
public class VectorOperationException extends BusinessException {

    private static final int HTTP_STATUS = 500;
    private static final String DEFAULT_MESSAGE = "向量操作失败";

    /**
     * 操作类型（存储、检索、删除等）
     */
    private final String operationType;

    /**
     * 关联的文档ID（可选）
     */
    private final UUID documentId;

    /**
     * 使用默认消息的构造函数
     */
    public VectorOperationException() {
        super(HTTP_STATUS, DEFAULT_MESSAGE);
        this.operationType = null;
        this.documentId = null;
    }

    /**
     * 带自定义消息的构造函数
     */
    public VectorOperationException(String message) {
        super(HTTP_STATUS, message);
        this.operationType = null;
        this.documentId = null;
    }

    /**
     * 带操作类型的构造函数
     */
    public VectorOperationException(String operationType, String message) {
        super(HTTP_STATUS,
            String.format("向量%s失败: %s", operationType, message));
        this.operationType = operationType;
        this.documentId = null;
    }

    /**
     * 带操作类型和文档ID的构造函数
     */
    public VectorOperationException(String operationType, UUID documentId, String message) {
        super(HTTP_STATUS,
            String.format("向量%s失败 [文档: %s]: %s", operationType, documentId, message));
        this.operationType = operationType;
        this.documentId = documentId;
    }

    /**
     * 带原因的构造函数
     */
    public VectorOperationException(String message, Throwable cause) {
        super(HTTP_STATUS, message, cause);
        this.operationType = null;
        this.documentId = null;
    }

    /**
     * 完整参数的构造函数
     */
    public VectorOperationException(String operationType, UUID documentId,
                                     String message, Throwable cause) {
        super(HTTP_STATUS,
            String.format("向量%s失败 [文档: %s]: %s", operationType, documentId, message),
            cause);
        this.operationType = operationType;
        this.documentId = documentId;
    }

    // ==================== Getter 方法 ====================

    public String getOperationType() {
        return operationType;
    }

    public UUID getDocumentId() {
        return documentId;
    }

    // ==================== 静态工厂方法 ====================

    /**
     * 创建向量存储异常
     */
    public static VectorOperationException storeError(UUID documentId,
                                                      String chunkId, Throwable cause) {
        return new VectorOperationException("存储", documentId,
            String.format("分块 %s 存储失败", chunkId), cause);
    }

    /**
     * 创建向量检索异常
     */
    public static VectorOperationException searchError(String query, Throwable cause) {
        return new VectorOperationException("检索", null,
            String.format("查询 '%s' 的相似度搜索失败", query), cause);
    }

    /**
     * 创建向量维度不匹配异常
     */
    public static VectorOperationException dimensionMismatch(int expected, int actual) {
        return new VectorOperationException("维度验证", null,
            String.format("向量维度不匹配，期望: %d，实际: %d", expected, actual));
    }

    /**
     * 创建向量删除异常
     */
    public static VectorOperationException deleteError(UUID documentId, Throwable cause) {
        return new VectorOperationException("删除", documentId,
            "删除向量记录失败", cause);
    }

    /**
     * 创建相似度计算异常
     */
    public static VectorOperationException similarityError(Throwable cause) {
        return new VectorOperationException("相似度计算", null,
            "计算向量相似度失败", cause);
    }

    /**
     * 创建向量化异常
     */
    public static VectorOperationException embeddingError(UUID documentId, String message) {
        return new VectorOperationException("向量化", documentId, message);
    }
}
