package com.elevn.mes.config;

import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;

import java.net.http.HttpClient;
import java.time.Duration;

/**
 * AI 接口 HTTP 超时配置
 */
@Configuration
public class HttpClientConfig {

    @Bean
    public RestClientCustomizer aiRestClientTimeoutCustomizer() {
        return builder -> {
            HttpClient httpClient = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))   // 建连超时：网络正常时 10 秒
                    .build();

            JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
            factory.setReadTimeout(Duration.ofMinutes(3));     // 读超时：从发出请求到收到完整响应,设置为3分钟应该足够了

            builder.requestFactory(factory);
        };
    }

}
