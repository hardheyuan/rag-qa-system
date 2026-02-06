package com.hiyuan.demo1.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * LLM 服务类 - 大语言模型调用服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LlmService {

    private final AiProviderModelManager modelManager;

    /**
     * 简单的文本生成
     */
    public String generate(String prompt) {
        log.debug("简单生成，Prompt 长度: {} 字符", prompt.length());
        
        try {
            return modelManager.getChatModel().generate(prompt);
        } catch (Exception e) {
            log.error("LLM 调用失败: {}", e.getMessage(), e);
            throw new RuntimeException("LLM 服务调用失败", e);
        }
    }

    /**
     * 生成问答答案
     */
    public String generateAnswer(String question, String context) {
        log.info("开始生成答案，问题: {}", question);
        log.debug("上下文长度: {} 字符", context != null ? context.length() : 0);
        
        try {
            String prompt = buildRagPrompt(question, context);
            long startTime = System.currentTimeMillis();
            String answer = modelManager.getChatModel().generate(prompt);
            long duration = System.currentTimeMillis() - startTime;
            
            log.info("答案生成完成，耗时: {}ms", duration);
            log.debug("答案长度: {} 字符", answer.length());
            
            return answer;
            
        } catch (Exception e) {
            log.error("LLM 调用失败: {}", e.getMessage(), e);
            return "抱歉，生成答案时遇到问题：" + e.getMessage();
        }
    }

    /**
     * 构建 RAG Prompt
     */
    private String buildRagPrompt(String question, String context) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("你是一个专业的教学助手。\n\n");
        
        if (context != null && !context.isEmpty()) {
            prompt.append("以下是相关的文档内容：\n");
            prompt.append("---\n");
            prompt.append(context);
            prompt.append("\n---\n\n");
        }
        
        prompt.append("用户问题：").append(question).append("\n\n");
        prompt.append("请基于上述文档内容回答用户的问题：");
        
        return prompt.toString();
    }

    /**
     * 检查 LLM 服务是否可用
     */
    public boolean isAvailable() {
        try {
            String response = modelManager.getChatModel().generate("Hello");
            return response != null && !response.isEmpty();
        } catch (Exception e) {
            log.warn("LLM 服务不可用: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 获取当前使用的模型信息
     */
    public String getModelInfo() {
        return modelManager.describeCurrentModel();
    }
}
