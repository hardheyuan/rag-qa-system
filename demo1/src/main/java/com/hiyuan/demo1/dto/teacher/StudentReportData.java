package com.hiyuan.demo1.dto.teacher;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 学生报表数据 DTO
 * 
 * 用于导出学生数据报表时的数据结构，包含学生基本信息和活动统计
 * 
 * 包含字段：
 * - username: 用户名
 * - email: 邮箱
 * - realName: 真实姓名
 * - totalQuestions: 总提问数
 * - totalDocumentAccesses: 总文档访问数
 * - lastActivity: 最后活动时间
 * 
 * @author 开发团队
 * @version 1.0.0
 * 
 * Requirements: 9.2
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentReportData {
    
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
     * 总提问数
     * 如果指定了日期范围，则只统计该范围内的提问数
     */
    private Long totalQuestions;
    
    /**
     * 总文档访问数
     * 如果指定了日期范围，则只统计该范围内的访问数
     */
    private Long totalDocumentAccesses;
    
    /**
     * 最后活动时间
     */
    private LocalDateTime lastActivity;
}
