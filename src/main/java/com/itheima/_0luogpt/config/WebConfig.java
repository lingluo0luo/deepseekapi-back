package com.itheima._0luogpt.config;

//import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 配置对 /tall 的跨域请求
        registry.addMapping("/tall")
                .allowedOrigins("http://localhost:5174")
                .allowedMethods("POST","OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);

        // 配置对 /api/generate-image 的跨域请求
        registry.addMapping("/api/generate-image/**")
                .allowedOrigins("http://localhost:5174")
                .allowedMethods("POST","OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);

        registry.addMapping("/api/history/**")
                .allowedOrigins("http://localhost:5174")
                .allowedMethods("GET", "POST", "OPTIONS") // 允许 POST
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
