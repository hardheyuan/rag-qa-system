package com.hiyuan.demo1.repository;

import com.hiyuan.demo1.entity.AiProviderConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AiProviderConfigRepository extends JpaRepository<AiProviderConfig, Long> {
    
    Optional<AiProviderConfig> findByIsActiveTrue();
    
    boolean existsByProviderCodeAndIsActiveTrue(String providerCode);
    
    Optional<AiProviderConfig> findByProviderCode(String providerCode);
}
