package com.hiyuan.demo1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * RAG 教学知识库问答系统 - 主启动类
 * 
 * 这是整个Spring Boot应用的入口点，相当于程序的"main函数"
 * 
 * 功能说明：
 * 1. 启动Spring Boot应用
 * 2. 自动扫描和配置所有组件（Controller、Service、Repository等）
 * 3. 启动内嵌的Tomcat服务器（默认8080端口）
 * 4. 启用异步处理功能（用于文档处理等耗时操作）
 * 
 * 注解说明：
 * @SpringBootApplication - Spring Boot的核心注解，包含了：
 *   - @Configuration: 标识这是一个配置类
 *   - @EnableAutoConfiguration: 启用自动配置
 *   - @ComponentScan: 自动扫描当前包及子包下的组件
 * 
 * @EnableAsync - 启用异步处理功能
 *   - 允许使用@Async注解创建异步方法
 *   - 用于文档上传后的后台处理，不阻塞用户请求
 * 
 * @author 开发团队
 * @version 1.0.0
 * @since 2026-01-09
 */
@SpringBootApplication
@EnableAsync           // 启用异步处理，支持@Async注解
public class Demo1Application {

    /**
     * 应用程序入口点
     * 
     * 当你运行 mvn spring-boot:run 或直接运行这个类时，
     * 程序会从这里开始执行
     * 
     * @param args 命令行参数（通常不需要）
     */
    public static void main(String[] args) {
        // 启动Spring Boot应用
        // 这行代码会：
        // 1. 创建Spring应用上下文
        // 2. 扫描并注册所有Bean（组件）
        // 3. 启动内嵌Tomcat服务器
        // 4. 应用就可以接收HTTP请求了
        SpringApplication.run(Demo1Application.class, args);
        
        // 启动成功后，你会在控制台看到类似这样的信息：
        // "Started Demo1Application in 3.2 seconds (JVM running for 3.8)"
        // 然后就可以通过 http://localhost:8080/api 访问应用了
    }
}
