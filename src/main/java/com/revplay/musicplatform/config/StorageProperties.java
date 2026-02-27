package com.revplay.musicplatform.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "revplay.storage")
public class StorageProperties {
    private String basePath = "storage";
}