package com.hiyuan.demo1.service;

import com.hiyuan.demo1.entity.AiProviderConfig;
import com.hiyuan.demo1.repository.AiProviderConfigRepository;
import com.hiyuan.demo1.util.AesEncryptionUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiProviderModelManagerTest {

    @Mock
    private AiProviderConfigRepository configRepository;

    @Mock
    private AesEncryptionUtil encryptionUtil;

    private AiProviderModelManager manager;

    @BeforeEach
    void setUp() {
        manager = new AiProviderModelManager(configRepository, encryptionUtil);

        ReflectionTestUtils.setField(manager, "defaultApiKey", "sk-default-key");
        ReflectionTestUtils.setField(manager, "defaultBaseUrl", "https://api.default/v1");
        ReflectionTestUtils.setField(manager, "defaultChatModel", "deepseek-ai/DeepSeek-V3");
        ReflectionTestUtils.setField(manager, "defaultTemperature", 0.7d);
        ReflectionTestUtils.setField(manager, "defaultMaxTokens", 4096);
    }

    @Test
    void usesDefaultConfigWhenNoActiveProvider() {
        when(configRepository.findByIsActiveTrue()).thenReturn(Optional.empty());

        AiProviderModelManager.CurrentProviderInfo info = manager.getCurrentProviderInfo();

        assertEquals("DEFAULT", info.providerCode());
        assertEquals("SiliconFlow (Default)", info.providerName());
        assertEquals("deepseek-ai/DeepSeek-V3", info.modelName());
        assertEquals("https://api.default/v1", info.baseUrl());
    }

    @Test
    void usesHigherDefaultMaxTokensToReduceAnswerTruncation() {
        when(configRepository.findByIsActiveTrue()).thenReturn(Optional.empty());

        AiProviderModelManager.ChatModelSettings settings = manager.getCurrentModelSettings();

        assertEquals(4096, settings.maxTokens());
    }

    @Test
    void refreshReloadsAfterActivation() {
        AiProviderConfig siliconFlow = AiProviderConfig.builder()
                .providerCode("SILICONFLOW")
                .providerName("硅基流动")
                .baseUrl("https://api.siliconflow.cn/v1")
                .apiKey("encrypted-1")
                .chatModel("deepseek-ai/DeepSeek-V3")
                .temperature(BigDecimal.valueOf(0.7))
                .maxTokens(2048)
                .isActive(true)
                .build();

        AiProviderConfig modelScope = AiProviderConfig.builder()
                .providerCode("MODELSCOPE")
                .providerName("魔搭社区")
                .baseUrl("https://api-inference.modelscope.cn/v1")
                .apiKey("encrypted-2")
                .chatModel("deepseek-ai/DeepSeek-R1")
                .temperature(BigDecimal.valueOf(0.8))
                .maxTokens(1024)
                .isActive(true)
                .build();

        when(configRepository.findByIsActiveTrue())
                .thenReturn(Optional.of(siliconFlow), Optional.of(modelScope));

        when(encryptionUtil.decrypt("encrypted-1")).thenReturn("plain-1");
        when(encryptionUtil.decrypt("encrypted-2")).thenReturn("plain-2");

        AiProviderModelManager.CurrentProviderInfo firstInfo = manager.getCurrentProviderInfo();
        assertEquals("SILICONFLOW", firstInfo.providerCode());

        AiProviderModelManager.CurrentProviderInfo cachedInfo = manager.getCurrentProviderInfo();
        assertEquals("SILICONFLOW", cachedInfo.providerCode());

        manager.refresh();
        AiProviderModelManager.CurrentProviderInfo refreshedInfo = manager.getCurrentProviderInfo();
        assertEquals("MODELSCOPE", refreshedInfo.providerCode());
    }

    @Test
    void upgradesLowConfiguredMaxTokensToAvoidEarlyCutoff() {
        AiProviderConfig modelScope = AiProviderConfig.builder()
                .providerCode("MODELSCOPE")
                .providerName("魔搭社区")
                .baseUrl("https://api-inference.modelscope.cn/v1")
                .apiKey("encrypted-2")
                .chatModel("deepseek-ai/DeepSeek-R1")
                .temperature(BigDecimal.valueOf(0.8))
                .maxTokens(1024)
                .isActive(true)
                .build();

        when(configRepository.findByIsActiveTrue()).thenReturn(Optional.of(modelScope));
        when(encryptionUtil.decrypt("encrypted-2")).thenReturn("plain-2");

        AiProviderModelManager.ChatModelSettings settings = manager.getCurrentModelSettings();

        assertEquals(4096, settings.maxTokens());
    }
}
