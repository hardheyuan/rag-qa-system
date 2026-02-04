package com.hiyuan.demo1.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 问答响应 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QaResponse {
    
    /**
     * 生成的答案
     */
    private String answer;
    
    /**
     * 相关文档引用列表
     */
    private List<CitationInfo> citations;
    
    /**
     * 问答历史ID
     */
    private String historyId;
    
    /**
     * 引用信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CitationInfo {
        /**
         * 文档ID
         */
        private String documentId;
        
        /**
         * 文档标题
         */
        private String documentTitle;
        
        /**
         * 相关内容片段
         */
        private String content;
        
        /**
         * 相似度分数
         */
        private Double score;
    }
}
