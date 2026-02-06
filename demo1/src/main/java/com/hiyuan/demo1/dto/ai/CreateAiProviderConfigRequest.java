package com.hiyuan.demo1.dto.ai;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateAiProviderConfigRequest {
    
    @NotBlank(message = "提供商代码不能为空")
    @Pattern(regexp = "^(SILICONFLOW|MODELSCOPE|NVIDIA)$", message = "不支持的提供商")
    private String providerCode;
    
    @NotBlank(message = "API基础URL不能为空")
    @Size(max = 500, message = "URL长度不能超过500字符")
    private String baseUrl;
    
    @NotBlank(message = "API密钥不能为空")
    @Size(max = 500, message = "API密钥长度不能超过500字符")
    private String apiKey;
    
    @NotBlank(message = "模型名称不能为空")
    @Size(max = 200, message = "模型名称长度不能超过200字符")
    private String chatModel;
    
    @NotNull(message = "温度参数不能为空")
    @DecimalMin(value = "0.0", message = "温度不能小于0")
    @DecimalMax(value = "2.0", message = "温度不能大于2")
    private BigDecimal temperature;
    
    @NotNull(message = "最大token数不能为空")
    @Min(value = 1, message = "最大token数必须大于0")
    @Max(value = 8192, message = "最大token数不能超过8192")
    private Integer maxTokens;
}
