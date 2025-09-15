package com.salgulok.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;

@Configuration
public class TourWebClientConfig {

    @Bean
    public WebClient tourApiWebClient() {
        DefaultUriBuilderFactory f = new DefaultUriBuilderFactory("https://apis.data.go.kr");
        f.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY); // ← 중요

        return WebClient.builder()
                .baseUrl("https://apis.data.go.kr")
                .build();
    }
}
