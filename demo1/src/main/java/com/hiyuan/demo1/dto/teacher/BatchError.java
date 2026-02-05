package com.hiyuan.demo1.dto.teacher;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 批量操作错误 DTO
 * 
 * 用于记录批量添加学生时的单个失败记录
 * 
 * @author 开发团队
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchError {
    
    /**
     * 学生标识符（用户名或邮箱）
     */
    private String identifier;
    
    /**
     * 失败原因
     */
    private String reason;
}
