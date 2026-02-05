package com.hiyuan.demo1.exception;

import lombok.Getter;

/**
 * 基础业务异常类
 * <p>
 * 所有业务相关的异常都应该继承此类，以便统一处理。
 * 提供了错误码、错误消息和详细信息等属性。
 * </p>
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * HTTP状态码
     */
    private final int code;

    /**
     * 错误详情（用于前端展示或日志记录）
     */
    private final String details;

    /**
     * 默认构造函数 - 400 Bad Request
     */
    public BusinessException(String message) {
        super(message);
        this.code = 400;
        this.details = null;
    }

    /**
     * 带错误码的构造函数
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
        this.details = null;
    }

    /**
     * 带错误码和详情的构造函数
     */
    public BusinessException(int code, String message, String details) {
        super(message);
        this.code = code;
        this.details = details;
    }

    /**
     * 带原因的构造函数
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.code = 400;
        this.details = cause != null ? cause.getMessage() : null;
    }

    /**
     * 带错误码和原因的构造函数
     */
    public BusinessException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.details = cause != null ? cause.getMessage() : null;
    }

    /**
     * 完整参数的构造函数
     */
    public BusinessException(int code, String message, String details, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.details = details;
    }

    // ==================== 静态工厂方法 ====================

    /**
     * 资源不存在异常 - 404 Not Found
     */
    public static BusinessException notFound(String resourceType, String identifier) {
        return new BusinessException(404,
            String.format("%s 不存在: %s", resourceType, identifier));
    }

    /**
     * 参数错误异常 - 400 Bad Request
     */
    public static BusinessException badRequest(String field, String reason) {
        return new BusinessException(400,
            String.format("参数错误 [%s]: %s", field, reason));
    }

    /**
     * 未授权异常 - 401 Unauthorized
     */
    public static BusinessException unauthorized(String reason) {
        return new BusinessException(401,
            "未授权访问: " + (reason != null ? reason : "请登录"));
    }

    /**
     * 禁止访问异常 - 403 Forbidden
     */
    public static BusinessException forbidden(String resource) {
        return new BusinessException(403,
            "禁止访问: " + (resource != null ? resource : "该资源"));
    }

    /**
     * 内部服务错误 - 500 Internal Server Error
     */
    public static BusinessException internalError(String operation, Throwable cause) {
        return new BusinessException(500,
            String.format("内部服务错误 [%s]", operation), cause);
    }

    /**
     * 服务不可用 - 503 Service Unavailable
     */
    public static BusinessException serviceUnavailable(String service) {
        return new BusinessException(503,
            String.format("服务暂时不可用: %s", service));
    }
}
