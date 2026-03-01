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
        String path = new File(uploadPath).getAbsolutePath();
        if (!path.endsWith(File.separator)) {
            path += File.separator;
        }
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + path);
    }
}
