package com.elevn.mes.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration // 配置类 实现接口 WebMvcConfigurer
public class CorsConfig implements WebMvcConfigurer {

    @Override // 重写接口中的默认方法
    public void addCorsMappings(CorsRegistry registry) {
        // 放行所有路径、所有来源——开发阶段先全放开，学起来不卡壳
        registry.addMapping("/**")                      // 所有接口路径
                .allowedOriginPatterns("*")             // 允许所有来源（协议+域名+端口任意）
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")  // 允许的请求方式
                .allowedHeaders("*")                    // 允许所有请求头
                .allowCredentials(true);                // 允许携带凭证（cookie/token，登录以后用）
    }
}