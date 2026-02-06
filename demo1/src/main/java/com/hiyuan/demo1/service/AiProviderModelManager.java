package com.hiyuan.demo1.service;

import com.hiyuan.demo1.entity.AiProviderConfig;
import com.hiyuan.demo1.repository.AiProviderConfigRepository;
import com.hiyuan.demo1.util.AesEncryptionUtil;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiProviderModelManager {

    private static final int MIN_CHAT_MAX_TOKENS = 4096;

    private final AiProviderConfigRepository configRepository;
    private final AesEncryptionUtil encryptionUtil;

    @Value("${langchain4j.modelscope.api-key:}")
    private String defaultApiKey;

    @Value("${langchain4j.modelscope.base-url:}")
    private String defaultBaseUrl;

    @Value("${langchain4j.modelscope.chat-model:}")
    private String defaultChatModel;

    @Value("${langchain4j.modelscope.temperature:0.7}")
    private double defaultTemperature;

    @Value("${langchain4j.modelscope.max-tokens:4096}")
    private int defaultMaxTokens;

    private final AtomicReference<ModelHolder> holder = new AtomicReference<>();

    public ChatLanguageModel getChatModel() {
        return ensureHolder().model();
    }

    public CurrentProviderInfo getCurrentProviderInfo() {
        ModelSettings settings = ensureHolder().settings();
        return new CurrentProviderInfo(
                settings.providerCode(),
                settings.providerName(),
                settings.modelName(),
                settings.baseUrl()
        );
    }

    public ChatModelSettings getCurrentModelSettings() {
        ModelSettings settings = ensureHolder().settings();
        return new ChatModelSettings(
                settings.baseUrl(),
                settings.modelName(),
                settings.apiKey(),
                settings.temperature(),
                settings.maxTokens()
        );
    }

    public String describeCurrentModel() {
        CurrentProviderInfo info = getCurrentProviderInfo();
        return info.providerName() + " - " + info.modelName();
    }

    public void refresh() {
        holder.set(null);
    }

    private ModelHolder ensureHolder() {
        ModelHolder existing = holder.get();
        if (existing != null) {
            return existing;
        }

        synchronized (this) {
            existing = holder.get();
            if (existing == null) {
                ModelSettings settings = resolveSettings();
                ChatLanguageModel model = buildModel(settings);
                existing = new ModelHolder(settings, model);
                holder.set(existing);
                log.info("AI提供商 [{}] 模型已加载", settings.providerCode());
            }
            return existing;
        }
    }

    private ModelSettings resolveSettings() {
        Optional<AiProviderConfig> activeConfig = configRepository.findByIsActiveTrue();
        if (activeConfig.isPresent()) {
            try {
                return fromEntity(activeConfig.get());
            } catch (Exception e) {
                log.error("加载数据库中的AI配置失败: {}，将回退到默认配置", e.getMessage());
            }
        }
        return defaultSettings();
    }

    private ModelSettings fromEntity(AiProviderConfig config) {
        String decrypted = encryptionUtil.decrypt(config.getApiKey());
        validateApiKey(decrypted, config.getProviderCode());

        double temperature = config.getTemperature() != null
                ? config.getTemperature().doubleValue()
                : defaultTemperature;
        int maxTokens = normalizeMaxTokens(config.getMaxTokens());

        return new ModelSettings(
                config.getProviderCode(),
                Objects.requireNonNullElse(config.getProviderName(), config.getProviderCode()),
                config.getBaseUrl(),
                config.getChatModel(),
                decrypted,
                temperature,
                maxTokens
        );
    }

    private ModelSettings defaultSettings() {
        validateApiKey(defaultApiKey, "DEFAULT");
        return new ModelSettings(
                "DEFAULT",
                "SiliconFlow (Default)",
                defaultBaseUrl,
                defaultChatModel,
                defaultApiKey,
                defaultTemperature,
                normalizeMaxTokens(defaultMaxTokens)
        );
    }

    private int normalizeMaxTokens(Integer configuredMaxTokens) {
        int resolved = configuredMaxTokens != null ? configuredMaxTokens : defaultMaxTokens;
        if (resolved < MIN_CHAT_MAX_TOKENS) {
            log.info("检测到maxTokens={}，为降低回答截断风险自动提升到{}", resolved, MIN_CHAT_MAX_TOKENS);
            return MIN_CHAT_MAX_TOKENS;
        }
        return resolved;
    }

    private ChatLanguageModel buildModel(ModelSettings settings) {
        return OpenAiChatModel.builder()
                .baseUrl(settings.baseUrl())
                .apiKey(settings.apiKey())
                .modelName(settings.modelName())
                .temperature(settings.temperature())
                .maxTokens(settings.maxTokens())
                .timeout(Duration.ofSeconds(60))
                .logRequests(false)
                .logResponses(false)
                .build();
    }

    private void validateApiKey(String apiKey, String providerCode) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("AI提供商 " + providerCode + " 的 API Key 未配置");
        }
    }

    private record ModelHolder(ModelSettings settings, ChatLanguageModel model) { }

    private record ModelSettings(
            String providerCode,
            String providerName,
            String baseUrl,
            String modelName,
            String apiKey,
            double temperature,
            int maxTokens
    ) { }

    public record CurrentProviderInfo(String providerCode, String providerName, String modelName, String baseUrl) { }

    public record ChatModelSettings(String baseUrl, String modelName, String apiKey, double temperature, int maxTokens) { }
}
