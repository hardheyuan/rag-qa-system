package com.hiyuan.demo1.dto.teacher;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 文档访问记录响应 DTO
 * 
 * 用于教师查看学生的文档访问记录
 * 
 * @author 开发团队
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentAccessResponse {
    
    /**
     * 文档ID
     */
    private UUID documentId;
    
    /**
     * 文档标题
     */
    private String documentTitle;
    
    /**
     * 访问次数
     */
    private Long accessCount;
    
    /**
     * 最后访问时间
     */
    private LocalDateTime lastAccessAt;
}
