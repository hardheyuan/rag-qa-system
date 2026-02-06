package com.hiyuan.demo1.dto.ai;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class AiProviderConfigResponse {
    private Long id;
    private String providerCode;
    private String providerName;
    private String baseUrl;
    private String apiKeyMasked;
    private String chatModel;
    private BigDecimal temperature;
    private Integer maxTokens;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
