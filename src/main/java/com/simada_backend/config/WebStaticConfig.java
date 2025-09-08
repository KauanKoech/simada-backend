package com.simada_backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebStaticConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadDir = Paths.get("uploads/avatars").toAbsolutePath().normalize();
        registry.addResourceHandler("/static/avatars/**")
                .addResourceLocations("file:" + uploadDir + "/");
    }
}
