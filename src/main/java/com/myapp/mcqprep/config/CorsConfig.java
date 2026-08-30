package com.myapp.mcqprep.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${miniapp.local.url}")
    private String miniappUrl;

    @Value("${miniapp.prod.url}")
    private String miniappUrlProd;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(miniappUrl, miniappUrlProd)
                .allowedMethods("GET", "POST");
    }
}