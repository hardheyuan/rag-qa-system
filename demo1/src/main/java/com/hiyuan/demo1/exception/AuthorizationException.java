package com.hiyuan.demo1.exception;

import java.util.UUID;

/**
 * 授权异常
 *
 * <p>当用户已认证但无权限执行某个操作时抛出此异常，例如：
 * <ul>
 *   <li>访问不属于自己的资源</li>
 *   <li>执行需要更高权限的操作</li>
 *   <li>角色权限不足</li>
 * </ul>
 * </p>
 */
public class AuthorizationException extends BusinessException {

    private static final int HTTP_STATUS = 403;
    private static final String DEFAULT_MESSAGE = "禁止访问";

    /**
     * 资源类型
     */
    private final String resourceType;

    /**
     * 资源ID
     */
    private final String resourceId;

    /**
     * 需要的权限
     */
    private final String requiredPermission;

    /**
     * 当前用户的权限
     */
    private final String currentPermission;

    /**
     * 使用默认消息的构造函数
     */
    public AuthorizationException() {
        super(HTTP_STATUS, DEFAULT_MESSAGE);
        this.resourceType = null;
        this.resourceId = null;
        this.requiredPermission = null;
        this.currentPermission = null;
    }

    /**
     * 带自定义消息的构造函数
     */
    public AuthorizationException(String message) {
        super(HTTP_STATUS, message);
        this.resourceType = null;
        this.resourceId = null;
        this.requiredPermission = null;
        this.currentPermission = null;
    }

    /**
     * 带资源和权限信息的构造函数
     */
    public AuthorizationException(String resourceType, String resourceId,
                                   String requiredPermission) {
        super(HTTP_STATUS,
            String.format("禁止访问 %s [%s]，需要权限: %s",
                resourceType, resourceId, requiredPermission));
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.requiredPermission = requiredPermission;
        this.currentPermission = null;
    }

    /**
     * 完整信息的构造函数
     */
    public AuthorizationException(String resourceType, String resourceId,
                                   String requiredPermission, String currentPermission) {
        super(HTTP_STATUS,
            String.format("禁止访问 %s [%s]，需要权限: %s，当前权限: %s",
                resourceType, resourceId, requiredPermission, currentPermission));
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.requiredPermission = requiredPermission;
        this.currentPermission = currentPermission;
    }

    /**
     * 带原因的构造函数
     */
    public AuthorizationException(String message, Throwable cause) {
        super(HTTP_STATUS, message, cause);
        this.resourceType = null;
        this.resourceId = null;
        this.requiredPermission = null;
        this.currentPermission = null;
    }

    // ==================== Getter 方法 ====================

    public String getResourceType() {
        return resourceType;
    }

    public String getResourceId() {
        return resourceId;
    }

    public String getRequiredPermission() {
        return requiredPermission;
    }

    public String getCurrentPermission() {
        return currentPermission;
    }

    // ==================== 静态工厂方法 ====================

    /**
     * 创建资源所有权验证失败的异常
     */
    public static AuthorizationException notOwner(String resourceType, UUID resourceId) {
        return new AuthorizationException(resourceType, resourceId.toString(), "资源所有者");
    }

    /**
     * 创建角色权限不足的异常
     */
    public static AuthorizationException insufficientRole(String currentRole,
                                                         String requiredRole) {
        AuthorizationException ex = new AuthorizationException(
            "角色", null, requiredRole, currentRole);
        return ex;
    }

    /**
     * 创建禁止访问特定操作的异常
     */
    public static AuthorizationException forbiddenAction(String action) {
        return new AuthorizationException(
            String.format("禁止执行操作: %s", action));
    }

    /**
     * 创建API访问限制异常
     */
    public static AuthorizationException apiLimitExceeded(String limitType) {
        return new AuthorizationException(
            String.format("超出API访问限制: %s", limitType));
    }
}
