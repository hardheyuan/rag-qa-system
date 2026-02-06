package com.hiyuan.demo1.dto.ai;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AiProviderStatusResponse {
    private boolean success;
    private String message;
    private Long responseTimeMs;
    private String providerCode;
    private String providerName;
}