package com.hiyuan.demo1.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * WebClient 配置类
 * 
 * 配置 HTTP 连接池和超时参数，解决连接被提前关闭的问题
 */
@Slf4j
@Configuration
public class WebClientConfig {

    /**
     * 配置 WebClient.Builder 使用自定义的 HttpClient
     * 包含连接池配置和超时设置
     */
    @Bean
    public WebClient.Builder webClientBuilder() {
        log.info("初始化 WebClient 配置...");
        
        // 配置连接池
        ConnectionProvider connectionProvider = ConnectionProvider.builder("siliconflow-api")
                .maxConnections(50)
                .pendingAcquireMaxCount(100)
                .pendingAcquireTimeout(Duration.ofSeconds(30))
                .maxIdleTime(Duration.ofSeconds(60))
                .maxLifeTime(Duration.ofMinutes(5))
                .evictInBackground(Duration.ofSeconds(30))
                .build();
        
        // 配置 HttpClient
        HttpClient httpClient = HttpClient.create(connectionProvider)
                // 连接超时：10秒
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                // 响应超时：60秒
                .responseTimeout(Duration.ofSeconds(60))
                // 配置读写超时
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(60, TimeUnit.SECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(30, TimeUnit.SECONDS)))
                // 保持连接，但设置 keep-alive 超时
                .keepAlive(true)
                // 禁用压缩（某些服务器可能有问题）
                .compress(false);
        
        log.info("WebClient 配置完成：连接池大小=50, 连接超时=10s, 响应超时=60s");
        
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient));
    }
}
