package com.rainbowmaodie.zjutbookingsystem.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${upload.path}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Map /uploads/** to the actual file path
        String path = uploadPath;
        if (!path.endsWith("/") && !path.endsWith("\\")) {
            path += File.separator;
        }

        // Windows 绝对路径映射必须以 file:/// 开头
        String resourceLocation = "file:///" + path.replace("\\", "/");
        
        registry.addResourceHandler("/uploads/**", "/api/uploads/**")
                .addResourceLocations(resourceLocation);
    }
}
