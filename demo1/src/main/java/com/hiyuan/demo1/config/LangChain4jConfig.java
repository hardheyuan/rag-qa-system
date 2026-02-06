package com.hiyuan.demo1.config;

import com.hiyuan.demo1.entity.AiProviderConfig;
import com.hiyuan.demo1.repository.AiProviderConfigRepository;
import com.hiyuan.demo1.service.EmbeddingService;
import com.hiyuan.demo1.util.AesEncryptionUtil;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.output.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * LangChain4j 配置类 - 支持多AI提供商切换
 *
 * 从数据库加载激活的配置，如果没有则使用application.yml默认值
 *
 * @author 开发团队
 * @version 3.1.0
 */
@Slf4j
@Configuration
public class LangChain4jConfig {

    @Value("${langchain4j.modelscope.api-key:}")
    private String defaultApiKey;

    @Value("${langchain4j.modelscope.base-url:}")
    private String defaultBaseUrl;

    @Value("${langchain4j.modelscope.chat-model:}")
    private String defaultChatModel;

    @Value("${langchain4j.modelscope.embedding-model:}")
    private String embeddingModel;

    @Value("${langchain4j.modelscope.temperature:0.7}")
    private double defaultTemperature;

    @Value("${langchain4j.modelscope.max-tokens:2048}")
    private int defaultMaxTokens;

    /**
     * 配置聊天语言模型 (ChatLanguageModel)
     * 优先从数据库加载配置，如果没有则使用application.yml默认值
     */
    @Bean
    public ChatLanguageModel chatLanguageModel(
            AiProviderConfigRepository configRepository,
            AesEncryptionUtil encryptionUtil) {
        
        // 尝试从数据库获取激活配置
        Optional<AiProviderConfig> activeConfig = configRepository.findByIsActiveTrue();
        
        if (activeConfig.isPresent()) {
            AiProviderConfig config = activeConfig.get();
            log.info("从数据库加载AI配置 - 提供商: {}, 模型: {}", 
                config.getProviderCode(), config.getChatModel());
            
            try {
                // 解密API密钥
                String decryptedApiKey = encryptionUtil.decrypt(config.getApiKey());
                
                double temperature = config.getTemperature() != null 
                    ? config.getTemperature().doubleValue() 
                    : 0.7;
                int maxTokens = config.getMaxTokens() != null 
                    ? config.getMaxTokens() 
                    : 2048;
                
                validateApiKey(decryptedApiKey);
                
                ChatLanguageModel model = OpenAiChatModel.builder()
                    .baseUrl(config.getBaseUrl())
                    .apiKey(decryptedApiKey)
                    .modelName(config.getChatModel())
                    .temperature(temperature)
                    .maxTokens(maxTokens)
                    .timeout(Duration.ofSeconds(60))
                    .logRequests(false)
                    .logResponses(false)
                    .build();

                log.info("ChatLanguageModel ({}) 初始化成功", config.getProviderCode());
                return model;
                
            } catch (Exception e) {
                log.error("从数据库加载配置失败: {}，回退到默认配置", e.getMessage());
            }
        }
        
        // 使用application.yml中的默认配置
        log.info("使用默认AI配置 - SiliconFlow (硅基流动)");
        log.info("Base URL: {}", defaultBaseUrl);
        log.info("模型: {}", defaultChatModel);

        validateApiKey(defaultApiKey);

        ChatLanguageModel model = OpenAiChatModel.builder()
            .apiKey(defaultApiKey)
            .baseUrl(defaultBaseUrl)
            .modelName(defaultChatModel)
            .temperature(defaultTemperature)
            .maxTokens(defaultMaxTokens)
            .timeout(Duration.ofSeconds(60))
            .logRequests(false)
            .logResponses(false)
            .build();

        log.info("ChatLanguageModel (SiliconFlow) 初始化成功");
        return model;
    }

    /**
     * 配置嵌入模型 (EmbeddingModel)
     * 使用 SiliconFlow 的 OpenAI 兼容接口（暂不支持切换）
     */
    @Bean
    public EmbeddingModel embeddingModel(EmbeddingService embeddingService) {
        log.info("初始化 EmbeddingModel - SiliconFlow (硅基流动)");
        log.info("嵌入模型: {}", embeddingModel);

        validateApiKey(defaultApiKey);

        EmbeddingModel model = new EmbeddingModel() {
            @Override
            public Response<Embedding> embed(String text) {
                float[] vector = embeddingService.embed(text);
                return Response.from(new Embedding(vector));
            }

            @Override
            public Response<Embedding> embed(TextSegment textSegment) {
                float[] vector = embeddingService.embed(textSegment.text());
                return Response.from(new Embedding(vector));
            }

            @Override
            public Response<List<Embedding>> embedAll(List<TextSegment> textSegments) {
                List<String> texts = textSegments.stream().map(TextSegment::text).toList();
                List<float[]> vectors = embeddingService.embedAll(texts);

                List<Embedding> embeddings = new ArrayList<>(vectors.size());
                for (float[] vector : vectors) {
                    embeddings.add(new Embedding(vector));
                }

                return Response.from(embeddings);
            }
        };

        log.info("EmbeddingModel (SiliconFlow) 初始化成功");
        return model;
    }
    
    /**
     * 验证 API Key 是否配置
     */
    private void validateApiKey(String apiKey) {
        if (apiKey == null || apiKey.isEmpty() || apiKey.startsWith("your-") || apiKey.equals("dummy-key")) {
            log.error("API Key 未配置或无效！");
            log.error("请在管理后台配置AI提供商，或在 application.yml 中配置 langchain4j.modelscope.api-key");
            throw new IllegalStateException("API Key 未配置");
        }
    }
}
