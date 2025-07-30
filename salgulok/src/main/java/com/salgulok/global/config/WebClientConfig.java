package com.salgulok.global.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;


@Configuration
public class WebClientConfig {
    @Value("${tourapi.url}")
    private String tourApiUrl;

    @Bean
    public WebClient webClient(@Value("${tourapi.url}") String tourApiUrl) {
        return WebClient.builder()
                .baseUrl(tourApiUrl)  // 환경변수에서 주입된 Base URL
                .build();
    }
}
