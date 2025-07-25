package com.devbattery.englishteacher.common.config;

import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class WebConfig implements WebMvcConfigurer {

    private final FileStorageProperties fileStorageProperties;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadDir = fileStorageProperties.getUploadDir();
        Path uploadPath = Paths.get(uploadDir);
        String resourceLocation = uploadPath.toUri().toString();

        registry.addResourceHandler(fileStorageProperties.getUploadUrlPrefix() + "**")
                .addResourceLocations(resourceLocation);
    }

}
