package com.hiyuan.demo1.dto.teacher;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 学生列表响应 DTO
 * 
 * 用于教师查看班级学生列表时返回的学生信息
 * 
 * @author 开发团队
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentListResponse {
    
    /**
     * 学生ID
     */
    private UUID id;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 真实姓名
     */
    private String realName;
    
    /**
     * 加入班级时间
     */
    private LocalDateTime enrolledAt;
    
    /**
     * 总提问数
     */
    private Long totalQuestions;
    
    /**
     * 最后活动时间
     */
    private LocalDateTime lastActivity;
}
