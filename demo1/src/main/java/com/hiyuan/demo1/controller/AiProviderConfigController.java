package com.hiyuan.demo1.controller;

import com.hiyuan.demo1.dto.ai.*;
import com.hiyuan.demo1.service.AiProviderConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/ai-providers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AiProviderConfigController {
    
    private final AiProviderConfigService configService;
    
    @GetMapping("/providers")
    public ResponseEntity<List<AiProviderInfo>> getSupportedProviders() {
        return ResponseEntity.ok(configService.getSupportedProviders());
    }
    
    @GetMapping
    public ResponseEntity<List<AiProviderConfigResponse>> getAllConfigs() {
        return ResponseEntity.ok(configService.getAllConfigs());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<AiProviderConfigResponse> getConfigById(@PathVariable Long id) {
        return configService.getConfigById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/current")
    public ResponseEntity<?> getCurrentConfig() {
        return configService.getActiveConfig()
            .map(config -> {
                Map<String, Object> response = new HashMap<>();
                response.put("id", config.getId());
                response.put("providerCode", config.getProviderCode());
                response.put("providerName", config.getProviderName());
                response.put("chatModel", config.getChatModel());
                response.put("isActive", config.getIsActive());
                return ResponseEntity.ok(response);
            })
            .orElse(ResponseEntity.ok(Map.of("message", "未配置AI提供商")));
    }
    
    @PostMapping
    public ResponseEntity<AiProviderConfigResponse> createConfig(
            @Valid @RequestBody CreateAiProviderConfigRequest request) {
        return ResponseEntity.ok(configService.createConfig(request));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<AiProviderConfigResponse> updateConfig(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAiProviderConfigRequest request) {
        return ResponseEntity.ok(configService.updateConfig(id, request));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteConfig(@PathVariable Long id) {
        configService.deleteConfig(id);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/{id}/activate")
    public ResponseEntity<Map<String, String>> activateConfig(@PathVariable Long id) {
        configService.activateConfig(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "配置已激活，请重启应用以应用更改");
        return ResponseEntity.ok(response);
    }
}
