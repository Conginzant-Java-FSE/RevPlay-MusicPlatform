package com.revplay.revplay.security;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@Component
@ConfigurationProperties(prefix = "revplay.jwt")
public class JwtProperties {

    @NotBlank
    private String secret;

    @Min(3_600_000)
    private long accessTokenExpiry = 86_400_000L;

    @Min(86_400_000)
    private long refreshTokenExpiry = 604_800_000L;

    @NotBlank
    private String issuer = "revplay";
}