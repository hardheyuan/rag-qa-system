package com.hiyuan.demo1.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiProviderInfo {
    private String code;
    private String name;
    private String defaultBaseUrl;
    private List<String> supportedModels;
}
