package org.kku.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class UploadConfig implements WebMvcConfigurer {
    @Value("${uploads.dir:uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String absPath = java.nio.file.Paths.get(uploadDir).toAbsolutePath().toString();
        if (!absPath.endsWith("/")) {
            absPath = absPath + "/";
        }
        String location = "file:" + absPath;
        registry.addResourceHandler("/uploads/**").addResourceLocations(location);
    }
}
