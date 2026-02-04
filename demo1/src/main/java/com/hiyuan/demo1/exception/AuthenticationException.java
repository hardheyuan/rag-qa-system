package com.hiyuan.demo1.exception;

/**
 * 认证异常
 *
 * <p>当用户身份验证失败时抛出此异常，例如：
 * <ul>
 *   <li>用户名或密码错误</li>
 *   <li>Token 无效或过期</li>
 *   <li>缺少认证信息</li>
 * </ul>
 * </p>
 */
public class AuthenticationException extends BusinessException {

    private static final int HTTP_STATUS = 401;
    private static final String DEFAULT_MESSAGE = "认证失败";

    /**
     * 认证类型（login、token、api_key等）
     */
    private final String authType;

    /**
     * 用户标识（可选，用于日志记录）
     */
    private final String username;

    /**
     * 使用默认消息的构造函数
     */
    public AuthenticationException() {
        super(HTTP_STATUS, DEFAULT_MESSAGE);
        this.authType = null;
        this.username = null;
    }

    /**
     * 带自定义消息的构造函数
     */
    public AuthenticationException(String message) {
        super(HTTP_STATUS, message);
        this.authType = null;
        this.username = null;
    }

    /**
     * 带认证类型的构造函数
     */
    public AuthenticationException(String authType, String message) {
        super(HTTP_STATUS,
            String.format("[%s] 认证失败: %s", authType, message));
        this.authType = authType;
        this.username = null;
    }

    /**
     * 带用户名的构造函数
     */
    public AuthenticationException(String authType, String username, String message) {
        super(HTTP_STATUS,
            String.format("[%s] 用户 '%s' 认证失败: %s", authType, username, message));
        this.authType = authType;
        this.username = username;
    }

    /**
     * 带原因的构造函数
     */
    public AuthenticationException(String message, Throwable cause) {
        super(HTTP_STATUS, message, cause);
        this.authType = null;
        this.username = null;
    }

    /**
     * 完整参数的构造函数
     */
    public AuthenticationException(String authType, String username,
                                   String message, Throwable cause) {
        super(HTTP_STATUS,
            String.format("[%s] 用户 '%s' 认证失败: %s", authType, username, message),
            cause);
        this.authType = authType;
        this.username = username;
    }

    // ==================== Getter 方法 ====================

    public String getAuthType() {
        return authType;
    }

    public String getUsername() {
        return username;
    }

    // ==================== 静态工厂方法 ====================

    /**
     * 创建用户名或密码错误的异常
     */
    public static AuthenticationException invalidCredentials(String username) {
        return new AuthenticationException("login", username,
            "用户名或密码错误");
    }

    /**
     * 创建Token过期的异常
     */
    public static AuthenticationException tokenExpired() {
        return new AuthenticationException("token", null,
            "登录已过期，请重新登录");
    }

    /**
     * 创建Token无效的异常
     */
    public static AuthenticationException invalidToken(String reason) {
        return new AuthenticationException("token", null,
            String.format("无效的令牌: %s", reason));
    }

    /**
     * 创建缺少认证信息的异常
     */
    public static AuthenticationException missingCredentials() {
        return new AuthenticationException("auth", null,
            "缺少认证信息，请提供有效的身份凭证");
    }

    /**
     * 创建账户被禁用的异常
     */
    public static AuthenticationException accountDisabled(String username) {
        return new AuthenticationException("login", username,
            "账户已被禁用，请联系管理员");
    }

    /**
     * 创建API Key无效的异常
     */
    public static AuthenticationException invalidApiKey() {
        return new AuthenticationException("api_key", null,
            "无效的API密钥");
    }
}
