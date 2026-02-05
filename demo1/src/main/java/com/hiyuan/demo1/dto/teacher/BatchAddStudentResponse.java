package com.hiyuan.demo1.dto.teacher;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 批量添加学生响应 DTO
 * 
 * 用于返回批量添加学生操作的结果摘要
 * 
 * @author 开发团队
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchAddStudentResponse {
    
    /**
     * 总处理数量
     */
    private int totalProcessed;
    
    /**
     * 成功添加数量
     */
    private int successCount;
    
    /**
     * 跳过数量（已存在的关联）
     */
    private int skippedCount;
    
    /**
     * 失败数量
     */
    private int failedCount;
    
    /**
     * 成功添加的学生标识符列表
     */
    private List<String> successList;
    
    /**
     * 跳过的学生标识符列表
     */
    private List<String> skippedList;
    
    /**
     * 失败的学生列表（包含失败原因）
     */
    private List<BatchError> failedList;
}
