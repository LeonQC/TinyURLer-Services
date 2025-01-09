package com.hkx.tinyurler.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**") // 指定允许跨域的路径
                        .allowedOrigins("http://localhost:3000", "http://localhost:5173", "http://frontend:3000") // 指定允许的前端地址
                        .allowedMethods("GET", "POST", "PUT", "DELETE",  "OPTIONS") // 指定允许的 HTTP 方法
                        .allowedHeaders("*") // 允许所有请求头
                        .allowCredentials(true); // 如果前端需要发送 Cookie 或认证信息
            }
        };
    }
}
