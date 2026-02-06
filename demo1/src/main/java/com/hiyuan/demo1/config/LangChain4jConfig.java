package com.hiyuan.demo1.config;

import com.hiyuan.demo1.service.EmbeddingService;
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

/**
 * LangChain4j 配置类 - 硅基流动 SiliconFlow 版本
 *
 * 使用 SiliconFlow 的 OpenAI 兼容接口
 * 支持各种开源模型：DeepSeek、GLM、Qwen 等
 *
 * @author 开发团队
 * @version 3.0.0
 */
@Slf4j
@Configuration
public class LangChain4jConfig {

    @Value("${langchain4j.modelscope.api-key}")
    private String apiKey;

    @Value("${langchain4j.modelscope.base-url}")
    private String baseUrl;

    @Value("${langchain4j.modelscope.chat-model}")
    private String chatModel;

    @Value("${langchain4j.modelscope.embedding-model}")
    private String embeddingModel;

    @Value("${langchain4j.modelscope.temperature:0.7}")
    private double temperature;

    @Value("${langchain4j.modelscope.max-tokens:2048}")
    private int maxTokens;

    /**
     * 配置聊天语言模型 (ChatLanguageModel)
     * 使用 SiliconFlow 的 OpenAI 兼容接口
     */
    @Bean
    public ChatLanguageModel chatLanguageModel() {
        log.info("初始化 ChatLanguageModel - SiliconFlow (硅基流动)");
        log.info("Base URL: {}", baseUrl);
        log.info("模型: {}", chatModel);

        validateApiKey();

        ChatLanguageModel model = OpenAiChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName(chatModel)
                .temperature(temperature)
                .maxTokens(maxTokens)
                .timeout(Duration.ofSeconds(60))
                .logRequests(false)
                .logResponses(false)
                .build();

        log.info("ChatLanguageModel (SiliconFlow) 初始化成功");
        return model;
    }

    /**
     * 配置嵌入模型 (EmbeddingModel)
     * 使用 SiliconFlow 的 OpenAI 兼容接口
     */
    @Bean
    public EmbeddingModel embeddingModel(EmbeddingService embeddingService) {
        log.info("初始化 EmbeddingModel - SiliconFlow (硅基流动)");
        log.info("嵌入模型: {}", embeddingModel);

        validateApiKey();

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
    private void validateApiKey() {
        if (apiKey == null || apiKey.isEmpty() || apiKey.startsWith("your-")) {
            log.error("SiliconFlow API Key 未配置！");
            log.error("请在 application.yml 中配置 langchain4j.modelscope.api-key");
            log.error("获取 API Key: https://cloud.siliconflow.cn/account/ak");
            throw new IllegalStateException("SiliconFlow API Key 未配置");
        }
    }
}
