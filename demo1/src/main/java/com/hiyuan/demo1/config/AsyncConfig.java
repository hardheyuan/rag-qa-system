package com.hiyuan.demo1.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 异步任务配置
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Value("${async.core-pool-size:2}")
    private int corePoolSize;

    @Value("${async.max-pool-size:5}")
    private int maxPoolSize;

    @Value("${async.queue-capacity:100}")
    private int queueCapacity;

    @Value("${async.thread-name-prefix:document-processor-}")
    private String threadNamePrefix;

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix(threadNamePrefix);
        executor.initialize();
        return executor;
    }
}
