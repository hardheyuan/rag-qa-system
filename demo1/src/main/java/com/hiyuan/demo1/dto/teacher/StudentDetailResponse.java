package com.hiyuan.demo1.dto.teacher;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 学生详情响应 DTO
 * 
 * 用于教师查看单个学生详细信息时返回的完整学生数据
 * 
 * @author 开发团队
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentDetailResponse {
    
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
     * 注册时间
     */
    private LocalDateTime registeredAt;
    
    /**
     * 加入班级时间
     */
    private LocalDateTime enrolledAt;
    
    /**
     * 总提问数
     */
    private Long totalQuestions;
    
    /**
     * 总文档访问数
     */
    private Long totalDocumentAccesses;
    
    /**
     * 最近活动摘要
     */
    private RecentActivitySummary recentActivity;
}
