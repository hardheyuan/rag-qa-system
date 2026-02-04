package com.hiyuan.demo1.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

/**
 * CORS 跨域配置类
 * 
 * 什么是CORS？
 * CORS (Cross-Origin Resource Sharing) 跨域资源共享
 * 
 * 为什么需要CORS配置？
 * 浏览器的同源策略限制：
 * - 前端运行在 http://localhost:3000 (React开发服务器)
 * - 后端运行在 http://localhost:8080 (Spring Boot)
 * - 端口不同，浏览器认为是"跨域"，会阻止请求
 * 
 * CORS配置的作用：
 * - 告诉浏览器："允许前端访问后端API"
 * - 配置允许的域名、请求方法、请求头等
 * - 处理浏览器的预检请求（OPTIONS请求）
 * 
 * 没有CORS配置会怎样？
 * - 前端调用后端API时会报错
 * - 浏览器控制台显示：Access to fetch at 'http://localhost:8080/api/...' 
 *   from origin 'http://localhost:3000' has been blocked by CORS policy
 * 
 * @author 开发团队
 * @version 1.0.0
 */
@Configuration  // 标识这是一个配置类
public class CorsConfig {

    /**
     * 配置 CORS 过滤器
     * 
     * 这个方法创建并配置一个CORS过滤器，它会拦截所有HTTP请求，
     * 检查是否是跨域请求，如果是则添加相应的CORS响应头
     * 
     * @return 配置好的CORS过滤器
     */
    @Bean  // 注册为Spring Bean，Spring会自动应用这个过滤器
    public CorsFilter corsFilter() {
        // 创建CORS配置对象
        CorsConfiguration config = new CorsConfiguration();

        // 1. 配置允许的来源（Origin）
        // 允许所有域名访问，包括：
        // - http://localhost:3000 (React开发服务器)
        // - http://localhost:8000 (静态文件服务器)
        // - file:// (直接打开HTML文件)
        // - 生产环境的域名
        config.setAllowedOriginPatterns(List.of("*"));
        
        // 注意：生产环境建议指定具体域名，如：
        // config.setAllowedOrigins(Arrays.asList(
        //     "http://localhost:3000",
        //     "https://your-frontend-domain.com"
        // ));

        // 2. 配置允许的HTTP请求方法
        // 支持常见的RESTful API方法
        config.setAllowedMethods(Arrays.asList(
                "GET",     // 查询数据 (获取文档列表、系统状态等)
                "POST",    // 创建数据 (上传文档、提问等)
                "PUT",     // 更新数据 (修改文档信息等)
                "DELETE",  // 删除数据 (删除文档、历史记录等)
                "OPTIONS", // 预检请求 (浏览器自动发送)
                "PATCH"    // 部分更新 (可能用到)
        ));

        // 3. 配置允许的请求头
        // 这些是前端可能发送的HTTP头
        config.setAllowedHeaders(Arrays.asList(
                "Origin",          // 请求来源
                "Content-Type",    // 内容类型 (application/json等)
                "Accept",          // 接受的响应类型
                "Authorization",   // 认证信息 (如果有用户登录)
                "X-User-Id",       // 自定义用户ID头 (我们的系统用到)
                "X-Requested-With" // AJAX请求标识
        ));

        // 4. 配置暴露给前端的响应头
        // 默认情况下，浏览器只能访问基本的响应头
        // 如果后端返回自定义头，需要在这里声明
        config.setExposedHeaders(Arrays.asList(
                "Content-Disposition", // 文件下载时的文件名
                "X-Suggested-Filename" // 建议的文件名
        ));

        // 5. 是否允许携带凭证（Cookie、认证信息等）
        // 设为true表示前端可以发送Cookie和认证头
        config.setAllowCredentials(true);

        // 6. 预检请求的缓存时间（秒）
        // 浏览器会缓存预检请求的结果，避免频繁发送OPTIONS请求
        // 3600秒 = 1小时
        config.setMaxAge(3600L);

        // 创建基于URL的CORS配置源
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        
        // 将CORS配置应用到所有路径 ("/**" 表示匹配所有URL)
        // 这意味着所有的API接口都会应用这个CORS配置
        source.registerCorsConfiguration("/**", config);

        // 创建并返回CORS过滤器
        return new CorsFilter(source);
    }
    
    /*
     * CORS工作流程说明：
     * 
     * 1. 简单请求（GET、POST等）：
     *    前端 -> 直接发送请求 -> 后端处理 -> 返回响应（带CORS头）
     * 
     * 2. 复杂请求（PUT、DELETE或自定义头）：
     *    前端 -> 发送OPTIONS预检请求 -> 后端返回允许的方法和头
     *         -> 前端发送实际请求 -> 后端处理 -> 返回响应
     * 
     * 3. 浏览器检查CORS头：
     *    - Access-Control-Allow-Origin: 检查来源是否允许
     *    - Access-Control-Allow-Methods: 检查方法是否允许
     *    - Access-Control-Allow-Headers: 检查请求头是否允许
     * 
     * 常见问题排查：
     * - 如果前端报CORS错误，检查allowedOrigins配置
     * - 如果OPTIONS请求失败，检查allowedMethods配置
     * - 如果自定义头被拒绝，检查allowedHeaders配置
     */
}
