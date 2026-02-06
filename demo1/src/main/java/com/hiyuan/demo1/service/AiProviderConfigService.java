package com.hiyuan.demo1.service;

import com.hiyuan.demo1.dto.ai.*;
import com.hiyuan.demo1.entity.AiProviderConfig;
import com.hiyuan.demo1.repository.AiProviderConfigRepository;
import com.hiyuan.demo1.util.AesEncryptionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiProviderConfigService {
    
    private final AiProviderConfigRepository configRepository;
    private final AesEncryptionUtil encryptionUtil;
    private final AiProviderModelManager modelManager;
    
    // 提供商预设信息
    private static final List<AiProviderInfo> PROVIDER_PRESETS = Arrays.asList(
        AiProviderInfo.builder()
            .code("SILICONFLOW")
            .name("硅基流动")
            .defaultBaseUrl("https://api.siliconflow.cn/v1")
            .supportedModels(Arrays.asList(
                "deepseek-ai/DeepSeek-V3",
                "Qwen/Qwen2.5-72B",
                "Qwen/Qwen2.5-32B",
                "deepseek-ai/DeepSeek-R1"
            ))
            .build(),
        AiProviderInfo.builder()
            .code("MODELSCOPE")
            .name("魔搭社区")
            .defaultBaseUrl("https://api-inference.modelscope.cn/v1")
            .supportedModels(Arrays.asList(
                "deepseek-ai/DeepSeek-R1",
                "Qwen/Qwen3-235B-A22B"
            ))
            .build(),
        AiProviderInfo.builder()
            .code("NVIDIA")
            .name("NVIDIA")
            .defaultBaseUrl("https://integrate.api.nvidia.com/v1")
            .supportedModels(Arrays.asList(
                "meta/llama-3.1-405b-instruct",
                "nvidia/nemotron-4-340b-instruct"
            ))
            .build()
    );
    
    public List<AiProviderInfo> getSupportedProviders() {
        return PROVIDER_PRESETS;
    }

    public List<String> getSupportedProviderTypes() {
        return PROVIDER_PRESETS.stream()
            .map(AiProviderInfo::getCode)
            .collect(Collectors.toList());
    }
    
    public List<AiProviderConfigResponse> getAllConfigs() {
        return configRepository.findAll().stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }
    
    public AiProviderConfigResponse getConfigById(Long id) {
        return configRepository.findById(id)
            .map(this::toResponse)
            .orElseThrow(() -> new RuntimeException("配置不存在: " + id));
    }
    
    public Optional<AiProviderConfig> getActiveConfig() {
        return configRepository.findByIsActiveTrue();
    }
    
    @Transactional
    public AiProviderConfigResponse createConfig(CreateAiProviderConfigRequest request) {
        // 获取提供商信息
        AiProviderInfo providerInfo = getProviderInfo(request.getProviderCode());
        
        // 加密API密钥
        String encryptedApiKey = encryptionUtil.encrypt(request.getApiKey());
        
        AiProviderConfig config = AiProviderConfig.builder()
            .providerCode(request.getProviderCode())
            .providerName(providerInfo.getName())
            .baseUrl(request.getBaseUrl())
            .apiKey(encryptedApiKey)
            .chatModel(request.getChatModel())
            .temperature(request.getTemperature())
            .maxTokens(request.getMaxTokens())
            .isActive(false)
            .build();
        
        AiProviderConfig saved = configRepository.save(config);
        log.info("Created AI provider config: id={}, provider={}", saved.getId(), saved.getProviderCode());
        
        return toResponse(saved);
    }
    
    @Transactional
    public AiProviderConfigResponse updateConfig(Long id, UpdateAiProviderConfigRequest request) {
        AiProviderConfig config = configRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("配置不存在: " + id));
        
        // 加密API密钥
        String encryptedApiKey = encryptionUtil.encrypt(request.getApiKey());
        
        config.setBaseUrl(request.getBaseUrl());
        config.setApiKey(encryptedApiKey);
        config.setChatModel(request.getChatModel());
        config.setTemperature(request.getTemperature());
        config.setMaxTokens(request.getMaxTokens());
        
        AiProviderConfig updated = configRepository.save(config);
        log.info("Updated AI provider config: id={}", updated.getId());
        if (Boolean.TRUE.equals(updated.getIsActive())) {
            modelManager.refresh();
        }
        
        return toResponse(updated);
    }
    
    @Transactional
    public void deleteConfig(Long id) {
        AiProviderConfig config = configRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("配置不存在: " + id));
        
        if (Boolean.TRUE.equals(config.getIsActive())) {
            throw new RuntimeException("不能删除当前激活的配置，请先激活其他配置");
        }
        
        configRepository.delete(config);
        log.info("Deleted AI provider config: id={}", id);
    }
    
    @Transactional
    public AiProviderConfigResponse activateConfig(Long id) {
        AiProviderConfig config = configRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("配置不存在: " + id));

        // 先禁用所有配置
        configRepository.findAll().forEach(c -> {
            c.setIsActive(false);
            configRepository.save(c);
        });

        // 激活指定配置
        config.setIsActive(true);
        configRepository.save(config);

        log.info("Activated AI provider config: id={}, provider={}", id, config.getProviderCode());
        modelManager.refresh();
        return toResponse(config);
    }
    
    private AiProviderConfigResponse toResponse(AiProviderConfig config) {
        return AiProviderConfigResponse.builder()
            .id(config.getId())
            .providerCode(config.getProviderCode())
            .providerName(config.getProviderName())
            .baseUrl(config.getBaseUrl())
            .apiKeyMasked(maskApiKey(config.getApiKey()))
            .chatModel(config.getChatModel())
            .temperature(config.getTemperature())
            .maxTokens(config.getMaxTokens())
            .isActive(config.getIsActive())
            .createdAt(config.getCreatedAt())
            .updatedAt(config.getUpdatedAt())
            .build();
    }
    
    private String maskApiKey(String encryptedApiKey) {
        try {
            String decrypted = encryptionUtil.decrypt(encryptedApiKey);
            if (decrypted.length() <= 8) {
                return "***" + decrypted.substring(decrypted.length() - 4);
            }
            return decrypted.substring(0, 3) + "..." + decrypted.substring(decrypted.length() - 4);
        } catch (Exception e) {
            log.warn("Failed to mask API key");
            return "***";
        }
    }
    
    private AiProviderInfo getProviderInfo(String providerCode) {
        return PROVIDER_PRESETS.stream()
            .filter(p -> p.getCode().equals(providerCode))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("不支持的提供商: " + providerCode));
    }

    public AiProviderStatusResponse testConnection(Long id) {
        AiProviderConfig config = configRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("配置不存在: " + id));

        long startTime = System.currentTimeMillis();

        try {
            // 解密API密钥
            String apiKey = encryptionUtil.decrypt(config.getApiKey());

            // 创建HTTP客户端
            HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

            // 构建简单的测试请求（获取模型列表或发送简单请求）
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(config.getBaseUrl() + "/models"))
                .header("Authorization", "Bearer " + apiKey)
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            long responseTime = System.currentTimeMillis() - startTime;

            if (response.statusCode() == 200) {
                return AiProviderStatusResponse.builder()
                    .success(true)
                    .message("连接成功")
                    .responseTimeMs(responseTime)
                    .providerCode(config.getProviderCode())
                    .providerName(config.getProviderName())
                    .build();
            } else {
                return AiProviderStatusResponse.builder()
                    .success(false)
                    .message("连接失败: HTTP " + response.statusCode())
                    .responseTimeMs(responseTime)
                    .providerCode(config.getProviderCode())
                    .providerName(config.getProviderName())
                    .build();
            }

        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            return AiProviderStatusResponse.builder()
                .success(false)
                .message("连接异常: " + e.getMessage())
                .responseTimeMs(responseTime)
                .providerCode(config.getProviderCode())
                .providerName(config.getProviderName())
                .build();
        }
    }
}
