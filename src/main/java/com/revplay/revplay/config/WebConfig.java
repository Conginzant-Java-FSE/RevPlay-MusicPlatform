package com.revplay.revplay.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final StorageProperties storageProperties;
    private final MediaProperties mediaProperties;

    public WebConfig(StorageProperties storageProperties, MediaProperties mediaProperties) {
        this.storageProperties = storageProperties;
        this.mediaProperties = mediaProperties;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path songsPath = Path.of(storageProperties.getBasePath(), "songs")
                .toAbsolutePath()
                .normalize();

        String handler = mediaProperties.getBaseUrl() + "/songs/**";

        registry.addResourceHandler(handler)
                .addResourceLocations(songsPath.toUri().toString());
    }
}