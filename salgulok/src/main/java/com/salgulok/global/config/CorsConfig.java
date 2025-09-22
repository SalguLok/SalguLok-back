package com.salgulok.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(
                    "https://salgulok-front.vercel.app",  // 배포용
                    "http://localhost:5173"               // 로컬 개발용
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
