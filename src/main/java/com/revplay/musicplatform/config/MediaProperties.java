package com.revplay.musicplatform.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "revplay.media")
public class MediaProperties {
    private String baseUrl = "/media";
}