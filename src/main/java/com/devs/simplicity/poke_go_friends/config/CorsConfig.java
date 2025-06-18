package com.devs.simplicity.poke_go_friends.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * CORS configuration for allowing requests from the Next.js frontend.
 * Configures allowed origins, methods, and headers for cross-origin requests.
 */
@Slf4j
@Configuration
public class CorsConfig {

    private final Environment environment;

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Value("${app.cors.allowed-methods}")
    private String allowedMethods;

    @Value("${app.cors.allowed-headers:Content-Type,Authorization,X-Requested-With}")
    private String allowedHeaders;

    @Value("${app.cors.allow-credentials:false}")
    private boolean allowCredentials;

    public CorsConfig(Environment environment) {
        this.environment = environment;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        boolean isProduction = Arrays.asList(environment.getActiveProfiles()).contains("prod");
        
        log.info("Configuring CORS for {} environment with origins: {} and methods: {}", 
                isProduction ? "production" : "development", allowedOrigins, allowedMethods);
        
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Parse allowed origins from comma-separated string
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        
        // In production, never allow wildcards
        if (isProduction) {
            boolean hasWildcard = origins.stream().anyMatch(origin -> origin.contains("*"));
            if (hasWildcard) {
                log.error("Wildcard origins are not allowed in production environment!");
                throw new IllegalStateException("Wildcard CORS origins are not allowed in production");
            }
        }
        
        configuration.setAllowedOrigins(origins);
        
        // Parse allowed methods from comma-separated string
        List<String> methods = Arrays.asList(allowedMethods.split(","));
        configuration.setAllowedMethods(methods);
        
        // Parse allowed headers from comma-separated string
        List<String> headers = Arrays.asList(allowedHeaders.split(","));
        configuration.setAllowedHeaders(headers);
        
        // Set credentials policy
        configuration.setAllowCredentials(allowCredentials);
        
        // Set max age for preflight requests (1 hour)
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        
        return source;
    }
}
