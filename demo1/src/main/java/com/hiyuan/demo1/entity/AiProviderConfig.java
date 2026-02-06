package com.hiyuan.demo1.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ai_provider_configs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiProviderConfig {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "provider_code", nullable = false, length = 50)
    private String providerCode;
    
    @Column(name = "provider_name", nullable = false, length = 100)
    private String providerName;
    
    @Column(name = "base_url", nullable = false, length = 500)
    private String baseUrl;
    
    @Column(name = "api_key", nullable = false, length = 500)
    private String apiKey;
    
    @Column(name = "chat_model", nullable = false, length = 200)
    private String chatModel;
    
    @Column(name = "temperature", precision = 3, scale = 2)
    private BigDecimal temperature;
    
    @Column(name = "max_tokens")
    private Integer maxTokens;
    
    @Column(name = "is_active")
    private Boolean isActive;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
