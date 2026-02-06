package com.hiyuan.demo1.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 向量化服务类 - 文本嵌入服务
 * 
 * 这个服务提供文本向量化功能，是 RAG 系统的核心组件之一。
 * 
 * 什么是向量化（Embedding）？
 * - 将文本转换为数值向量（如 384 维或 768 维的浮点数数组）
 * - 语义相似的文本会有相似的向量表示
 * - 可以通过计算向量距离来衡量文本相似度
 * 
 * 在 RAG 系统中的作用：
 * 1. 文档处理时：将文档分块后向量化，存入向量数据库
 * 2. 问答时：将用户问题向量化，搜索最相似的文档分块
 * 
 * @author 开发团队
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmbeddingService {

    private final WebClient.Builder webClientBuilder;

    @Value("${langchain4j.modelscope.api-key}")
    private String apiKey;

    @Value("${langchain4j.modelscope.base-url}")
    private String baseUrl;

    @Value("${langchain4j.modelscope.embedding-model}")
    private String embeddingModel;

    private WebClient client() {
        return webClientBuilder
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .build();
    }

    /**
     * 向量化单个文本（带重试机制）
     * 
     * 将一段文本转换为向量表示，遇到限流时会自动重试
     * 
     * @param text 要向量化的文本
     * @return 向量数组（浮点数数组）
     */
    public float[] embed(String text) {
        if (text == null || text.trim().isEmpty()) {
            log.warn("尝试向量化空文本");
            throw new IllegalArgumentException("文本不能为空");
        }
        
        log.debug("向量化文本，长度: {} 字符", text.length());
        
        // 重试配置：最多3次，指数退避
        int maxRetries = 3;
        int baseDelayMs = 1000; // 基础延迟1秒
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                long startTime = System.currentTimeMillis();
                
                // 接受任何类型的响应，包括 text/plain
                String responseBody = client()
                        .post()
                        .uri("/embeddings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .bodyValue(Map.of(
                                "model", embeddingModel,
                                "input", text,
                                "encoding_format", "float"))
                        .retrieve()
                        .bodyToMono(String.class)
                        .timeout(Duration.ofSeconds(60))
                        .block();

                if (responseBody == null || responseBody.isEmpty()) {
                    throw new RuntimeException("嵌入接口返回空响应");
                }
                
                log.debug("嵌入API响应: {}", responseBody.length() > 500 ? responseBody.substring(0, 500) + "..." : responseBody);

                // 使用 ObjectMapper 解析响应
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                JsonNode json = mapper.readTree(responseBody);

                // 检查是否有错误（包括限流错误）
                if (json.has("errors") || json.has("error")) {
                    JsonNode errorNode = json.has("errors") ? json.path("errors") : json.path("error");
                    String errorMsg = errorNode.path("message").asText(errorNode.asText());
                    
                    // 检查是否是限流错误
                    if (isRateLimitError(errorMsg)) {
                        if (attempt < maxRetries) {
                            int delayMs = baseDelayMs * (int) Math.pow(2, attempt - 1); // 1s, 2s, 4s
                            log.warn("遇到API限流，第{}次尝试失败，等待{}ms后重试...", attempt, delayMs);
                            Thread.sleep(delayMs);
                            continue; // 继续下一次重试
                        }
                    }
                    
                    throw new RuntimeException("嵌入API错误: " + errorMsg);
                }

                JsonNode embeddingNode = json.path("data").path(0).path("embedding");
                if (!embeddingNode.isArray() || embeddingNode.isEmpty()) {
                    log.error("嵌入响应格式异常，完整响应: {}", responseBody);
                    throw new RuntimeException("嵌入接口响应缺少 embedding 字段，请检查模型名称是否正确: " + embeddingModel);
                }

                float[] vector = new float[embeddingNode.size()];
                for (int i = 0; i < embeddingNode.size(); i++) {
                    vector[i] = (float) embeddingNode.get(i).asDouble();
                }
                
                long duration = System.currentTimeMillis() - startTime;
                log.info("向量化完成，维度: {}, 耗时: {}ms", vector.length, duration);
                
                return vector;
                
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("向量化被中断", ie);
            } catch (Exception e) {
                if (attempt < maxRetries && isRetryableError(e)) {
                    int delayMs = baseDelayMs * (int) Math.pow(2, attempt - 1);
                    log.warn("向量化失败，第{}次尝试，等待{}ms后重试: {}", attempt, delayMs, e.getMessage());
                    try {
                        Thread.sleep(delayMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("向量化被中断", ie);
                    }
                } else {
                    log.error("向量化失败（已重试{}次）: {}", attempt, e.getMessage(), e);
                    throw new RuntimeException("文本向量化失败: " + e.getMessage(), e);
                }
            }
        }
        
        throw new RuntimeException("向量化失败：已达到最大重试次数");
    }
    
    /**
     * 检查是否是限流错误
     */
    private boolean isRateLimitError(String errorMessage) {
        if (errorMessage == null) return false;
        String lowerMsg = errorMessage.toLowerCase();
        return lowerMsg.contains("rate limit") || 
               lowerMsg.contains("rate_limit") ||
               lowerMsg.contains("too many requests") ||
               lowerMsg.contains("limit") && lowerMsg.contains("higher");
    }
    
    /**
     * 检查错误是否可重试
     */
    private boolean isRetryableError(Exception e) {
        String msg = e.getMessage();
        if (msg == null) return false;
        String lowerMsg = msg.toLowerCase();
        return lowerMsg.contains("rate limit") ||
               lowerMsg.contains("timeout") ||
               lowerMsg.contains("connection") ||
               lowerMsg.contains("503") ||
               lowerMsg.contains("429");
    }

    /**
     * 批量向量化文本（带重试机制）
     * 
     * 将多段文本批量转换为向量，比逐个调用更高效，遇到限流会自动重试
     * 
     * @param texts 要向量化的文本列表
     * @return 向量列表
     */
    public List<float[]> embedAll(List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            log.warn("尝试批量向量化空列表");
            return new ArrayList<>();
        }
        
        log.info("批量向量化 {} 个文本", texts.size());
        
        // 重试配置
        int maxRetries = 3;
        int baseDelayMs = 1000;
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                long startTime = System.currentTimeMillis();
                
                // 接受任何类型的响应，包括 text/plain
                String responseBody = client()
                        .post()
                        .uri("/embeddings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .bodyValue(Map.of(
                                "model", embeddingModel,
                                "input", texts,
                                "encoding_format", "float"))
                        .retrieve()
                        .bodyToMono(String.class)
                        .timeout(Duration.ofSeconds(60))
                        .block();

                if (responseBody == null || responseBody.isEmpty()) {
                    throw new RuntimeException("嵌入接口返回空响应");
                }

                // 使用 ObjectMapper 解析响应
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                JsonNode json = mapper.readTree(responseBody);

                // 检查是否有错误（包括限流错误）
                if (json.has("errors") || json.has("error")) {
                    JsonNode errorNode = json.has("errors") ? json.path("errors") : json.path("error");
                    String errorMsg = errorNode.path("message").asText(errorNode.asText());
                    
                    // 检查是否是限流错误
                    if (isRateLimitError(errorMsg)) {
                        if (attempt < maxRetries) {
                            int delayMs = baseDelayMs * (int) Math.pow(2, attempt - 1);
                            log.warn("批量向量化遇到API限流，第{}次尝试失败，等待{}ms后重试...", attempt, delayMs);
                            Thread.sleep(delayMs);
                            continue;
                        }
                    }
                    
                    throw new RuntimeException("嵌入API错误: " + errorMsg);
                }

                JsonNode dataNode = json.path("data");
                if (!dataNode.isArray()) {
                    log.error("批量嵌入响应格式异常: {}", responseBody.length() > 1000 ? responseBody.substring(0, 1000) : responseBody);
                    throw new RuntimeException("嵌入接口响应缺少 data 字段");
                }

                List<float[]> vectors = new ArrayList<>();
                for (int idx = 0; idx < dataNode.size(); idx++) {
                    JsonNode embeddingNode = dataNode.get(idx).path("embedding");
                    if (!embeddingNode.isArray()) {
                        throw new RuntimeException("嵌入接口响应缺少 embedding 字段");
                    }
                    float[] vector = new float[embeddingNode.size()];
                    for (int i = 0; i < embeddingNode.size(); i++) {
                        vector[i] = (float) embeddingNode.get(i).asDouble();
                    }
                    vectors.add(vector);
                }
                
                long duration = System.currentTimeMillis() - startTime;
                log.info("批量向量化完成，数量: {}, 总耗时: {}ms, 平均: {}ms/个", 
                        vectors.size(), duration, duration / texts.size());
                
                return vectors;
                
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("批量向量化被中断", ie);
            } catch (Exception e) {
                if (attempt < maxRetries && isRetryableError(e)) {
                    int delayMs = baseDelayMs * (int) Math.pow(2, attempt - 1);
                    log.warn("批量向量化失败，第{}次尝试，等待{}ms后重试: {}", attempt, delayMs, e.getMessage());
                    try {
                        Thread.sleep(delayMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("批量向量化被中断", ie);
                    }
                } else {
                    log.error("批量向量化失败（已重试{}次）: {}", attempt, e.getMessage(), e);
                    throw new RuntimeException("批量文本向量化失败: " + e.getMessage(), e);
                }
            }
        }
        
        throw new RuntimeException("批量向量化失败：已达到最大重试次数");
    }

    /**
     * 获取向量维度
     * 
     * 不同的嵌入模型有不同的向量维度：
     * - all-MiniLM-L6-v2: 384 维
     * - all-mpnet-base-v2: 768 维
     * 
     * @return 向量维度
     */
    public int getDimension() {
        // 通过嵌入一个测试文本来获取维度
        try {
            float[] testVector = embed("test");
            return testVector.length;
        } catch (Exception e) {
            log.warn("无法获取向量维度: {}", e.getMessage());
            return 384; // 默认维度
        }
    }

    /**
     * 检查嵌入服务是否可用
     * 
     * @return true 表示服务可用
     */
    public boolean isAvailable() {
        try {
            float[] vector = embed("test");
            return vector != null && vector.length > 0;
        } catch (Exception e) {
            log.warn("嵌入服务不可用: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 获取模型信息
     *
     * @return 模型信息描述
     */
    public String getModelInfo() {
        return "SiliconFlow OpenAI-Compatible Embedding";
    }
}
