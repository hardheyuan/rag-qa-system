-- 创建AI提供商配置表
CREATE TABLE IF NOT EXISTS ai_provider_configs (
    id BIGSERIAL PRIMARY KEY,
    provider_code VARCHAR(50) NOT NULL,
    provider_name VARCHAR(100) NOT NULL,
    base_url VARCHAR(500) NOT NULL,
    api_key VARCHAR(500) NOT NULL,
    chat_model VARCHAR(200) NOT NULL,
    temperature DECIMAL(3,2) DEFAULT 0.7,
    max_tokens INTEGER DEFAULT 2048,
    is_active BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- 约束
    CONSTRAINT chk_temperature CHECK (temperature >= 0.0 AND temperature <= 2.0),
    CONSTRAINT chk_max_tokens CHECK (max_tokens > 0 AND max_tokens <= 8192)
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_ai_provider_configs_active ON ai_provider_configs(is_active);
CREATE INDEX IF NOT EXISTS idx_ai_provider_configs_provider ON ai_provider_configs(provider_code);

-- 插入默认配置（硅基流动）- 使用占位符，需要在应用启动后通过API更新真实的API密钥
-- 注意：这里的api_key是一个加密的占位符，实际使用时需要通过管理界面配置
INSERT INTO ai_provider_configs (
    provider_code, 
    provider_name, 
    base_url, 
    api_key, 
    chat_model, 
    temperature, 
    max_tokens, 
    is_active
) VALUES (
    'SILICONFLOW',
    '硅基流动',
    'https://api.siliconflow.cn/v1',
    'placeholder-encrypted-key',
    'deepseek-ai/DeepSeek-V3',
    0.7,
    2048,
    FALSE
) ON CONFLICT DO NOTHING;

-- 添加注释
COMMENT ON TABLE ai_provider_configs IS 'AI提供商配置表';
COMMENT ON COLUMN ai_provider_configs.provider_code IS '提供商代码: SILICONFLOW, MODELSCOPE, NVIDIA';
COMMENT ON COLUMN ai_provider_configs.api_key IS 'AES加密的API密钥';
COMMENT ON COLUMN ai_provider_configs.is_active IS '是否当前激活（全局只有一个TRUE）';
