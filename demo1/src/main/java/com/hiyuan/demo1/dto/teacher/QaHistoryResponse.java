package com.hiyuan.demo1.dto.teacher;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 问答历史响应 DTO
 * 
 * 用于教师查看学生问答历史记录
 * 
 * @author 开发团队
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QaHistoryResponse {
    
    /**
     * 问答记录ID
     */
    private UUID id;
    
    /**
     * 问题内容
     */
    private String question;
    
    /**
     * 答案内容
     */
    private String answer;
    
    /**
     * 提问时间
     */
    private LocalDateTime askedAt;
}
