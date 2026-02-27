package com.revplay.musicplatform.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler()))
                .headers(headers -> headers
                        .frameOptions(frame -> frame.deny())
                        .xssProtection(xss -> xss.disable())
                        .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'"))
                        .referrerPolicy(ref ->
                                ref.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER)))
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/api/v1/users/password-reset/**").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/error").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/v1/users/profile/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/v1/users/profile/**").authenticated()
                        .requestMatchers("/api/v1/admin/users/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/artists/**", "/api/v1/songs/**", "/api/v1/albums/**"
                        ).authenticated()
                        .requestMatchers(HttpMethod.POST,
                                "/api/v1/artists/**", "/api/v1/songs/**", "/api/v1/albums/**"
                        ).hasAnyRole("ARTIST", "ADMIN")
                        .requestMatchers(HttpMethod.PUT,
                                "/api/v1/artists/**", "/api/v1/songs/**", "/api/v1/albums/**"
                        ).hasAnyRole("ARTIST", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE,
                                "/api/v1/artists/**", "/api/v1/songs/**", "/api/v1/albums/**"
                        ).hasAnyRole("ARTIST", "ADMIN")

                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/search/**", "/api/v1/genres/**",
                                "/api/v1/podcasts/**", "/api/v1/podcast-episodes/**"
                        ).authenticated()
                        .requestMatchers(HttpMethod.POST,
                                "/api/v1/podcasts/**", "/api/v1/podcast-episodes/**"
                        ).hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,
                                "/api/v1/podcasts/**", "/api/v1/podcast-episodes/**"
                        ).hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE,
                                "/api/v1/podcasts/**", "/api/v1/podcast-episodes/**"
                        ).hasRole("ADMIN")

                        .requestMatchers("/api/v1/queue/**", "/api/v1/play-history/**").authenticated()

                        .requestMatchers("/api/v1/playlists/**", "/api/v1/likes/**").authenticated()
                        .requestMatchers("/api/v1/analytics/artist/**").hasAnyRole("ARTIST", "ADMIN")
                        .requestMatchers("/api/v1/admin/audit-logs/**").hasRole("ADMIN")

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (HttpServletRequest req, HttpServletResponse res, AuthenticationException ex) -> {
            res.setStatus(HttpStatus.UNAUTHORIZED.value());
            res.setContentType(MediaType.APPLICATION_JSON_VALUE);
            writeErrorResponse(res, 401, "Unauthorized",
                    "Authentication required. Please provide a valid Bearer token.",
                    req.getRequestURI());
        };
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (HttpServletRequest req, HttpServletResponse res, AccessDeniedException ex) -> {
            res.setStatus(HttpStatus.FORBIDDEN.value());
            res.setContentType(MediaType.APPLICATION_JSON_VALUE);
            writeErrorResponse(res, 403, "Forbidden",
                    "You do not have permission to access this resource.",
                    req.getRequestURI());
        };
    }

    private void writeErrorResponse(HttpServletResponse res, int status,
                                    String error, String message, String path) {
        try {
            String body = "{"
                    + "\"status\":" + status + ","
                    + "\"error\":\"" + error + "\","
                    + "\"message\":\"" + message + "\","
                    + "\"path\":\"" + path + "\","
                    + "\"timestamp\":\"" + Instant.now() + "\""
                    + "}";
            res.getWriter().write(body);
            res.getWriter().flush();
        } catch (IOException e) {
            res.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}