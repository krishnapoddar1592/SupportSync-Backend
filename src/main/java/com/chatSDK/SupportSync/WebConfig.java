package com.chatSDK.SupportSync;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/chat.startSession")
                .allowedOrigins("http://localhost:8081/","http://localhost:8082/")
                .allowedMethods("POST");
    }
}


