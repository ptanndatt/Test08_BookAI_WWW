package com.iuh.bookstore.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadDir = Paths.get("uploads/book-covers");
        String uploadPath = uploadDir.toFile().getAbsolutePath();
        registry.addResourceHandler("/book-covers/**")
                .addResourceLocations("file:" + uploadPath + "/");
    }
}
