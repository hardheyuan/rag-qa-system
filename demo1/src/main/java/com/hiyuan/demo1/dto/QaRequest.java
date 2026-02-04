package com.hiyuan.demo1.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 问答请求 DTO
 */
@Data
public class QaRequest {
    
    /**
     * 用户问题
     */
    @NotBlank(message = "问题不能为空")
    private String question;
    
    /**
     * 用户ID（可选，字符串类型）
     */
    private String userId;
    
    /**
     * 检索相关文档数量（可选，默认5）
     */
    private Integer topK = 5;
}
