package com.hiyuan.demo1.dto.teacher;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 最近活动摘要 DTO
 * 
 * 用于展示学生最近30天的活动统计
 * 
 * @author 开发团队
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentActivitySummary {
    
    /**
     * 最近30天提问数
     */
    private Long questionsLast30Days;
    
    /**
     * 最近30天文档访问数
     */
    private Long documentAccessesLast30Days;
    
    /**
     * 最后提问时间
     */
    private LocalDateTime lastQuestionAt;
    
    /**
     * 最后文档访问时间
     */
    private LocalDateTime lastDocumentAccessAt;
}
