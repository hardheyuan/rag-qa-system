package com.hiyuan.demo1.exception;

import com.hiyuan.demo1.dto.response.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException ex) {
        log.warn("业务异常: {}", ex.getMessage());
        return ResponseEntity
                .status(ex.getCode())
                .body(ApiResponse.error(ex.getCode(), ex.getMessage()));
    }

    /**
     * 处理文档不存在异常
     */
    @ExceptionHandler(DocumentNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleDocumentNotFoundException(DocumentNotFoundException ex) {
        log.warn("文档不存在: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(404, ex.getMessage()));
    }

    /**
     * 处理文档处理异常
     */
    @ExceptionHandler(DocumentProcessingException.class)
    public ResponseEntity<ApiResponse<Void>> handleDocumentProcessingException(DocumentProcessingException ex) {
        log.error("文档处理失败: {}", ex.getMessage());
        if (ex.getDocumentId() != null) {
            log.error("关联文档ID: {}, 处理阶段: {}", ex.getDocumentId(), ex.getProcessingStage());
        }
        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ApiResponse.error(422, ex.getMessage()));
    }

    /**
     * 处理向量操作异常
     */
    @ExceptionHandler(VectorOperationException.class)
    public ResponseEntity<ApiResponse<Void>> handleVectorOperationException(VectorOperationException ex) {
        log.error("向量操作失败: {}", ex.getMessage());
        if (ex.getDocumentId() != null) {
            log.error("关联文档ID: {}, 操作类型: {}", ex.getDocumentId(), ex.getOperationType());
        }
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(500, "向量处理服务暂时不可用，请稍后重试"));
    }

    /**
     * 处理认证异常
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(AuthenticationException ex) {
        log.warn("认证失败: {}", ex.getMessage());
        if (ex.getUsername() != null) {
            log.warn("尝试登录的用户: {}", ex.getUsername());
        }
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(401, ex.getMessage()));
    }

    /**
     * 处理授权异常
     */
    @ExceptionHandler(AuthorizationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthorizationException(AuthorizationException ex) {
        log.warn("授权失败: {}", ex.getMessage());
        if (ex.getResourceType() != null && ex.getResourceId() != null) {
            log.warn("尝试访问的资源: {} [{}]", ex.getResourceType(), ex.getResourceId());
        }
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(403, ex.getMessage()));
    }

    /**
     * 处理参数校验异常（@Valid）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        log.warn("参数校验失败: {}", errors);
        return ResponseEntity
                .badRequest()
                .body(ApiResponse.error(400, "参数校验失败"));
    }

    /**
     * 处理约束违反异常
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolationException(
            ConstraintViolationException ex) {
        log.warn("约束违反: {}", ex.getMessage());
        return ResponseEntity
                .badRequest()
                .body(ApiResponse.badRequest(ex.getMessage()));
    }

    /**
     * 处理缺少请求参数异常
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingParameterException(
            MissingServletRequestParameterException ex) {
        String message = String.format("缺少必需参数: %s", ex.getParameterName());
        log.warn(message);
        return ResponseEntity
                .badRequest()
                .body(ApiResponse.badRequest(message));
    }

    /**
     * 处理文件上传大小超限异常
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException ex) {
        log.warn("文件上传超过大小限制: {}", ex.getMessage());
        return ResponseEntity
                .badRequest()
                .body(ApiResponse.badRequest("文件大小超过限制（最大 50MB）"));
    }

    /**
     * 处理 404 异常
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoHandlerFoundException(
            NoHandlerFoundException ex) {
        log.warn("请求路径不存在: {}", ex.getRequestURL());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.notFound("请求的资源不存在"));
    }

    /**
     * 处理静态资源未找到异常 (Spring Boot 3.x)
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResourceFoundException(
            NoResourceFoundException ex) {
        log.warn("静态资源不存在: {}", ex.getResourcePath());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.notFound("请求的资源不存在: " + ex.getResourcePath()));
    }

    /**
     * 处理其他未知异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        log.error("服务器内部错误", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.serverError("服务器内部错误，请稍后重试"));
    }
}
