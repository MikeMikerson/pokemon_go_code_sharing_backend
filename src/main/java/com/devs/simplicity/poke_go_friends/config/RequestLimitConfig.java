package com.devs.simplicity.poke_go_friends.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration for request size limits and other web-related settings.
 */
@Configuration
public class RequestLimitConfig implements WebMvcConfigurer {

    @Value("${app.request.max-size-mb:1}")
    private int maxRequestSizeMb;

    @Value("${app.request.max-file-size-mb:1}")
    private int maxFileSizeMb;

    /**
     * Configures Tomcat with request size limits.
     */
    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> tomcatCustomizer() {
        return factory -> factory.addConnectorCustomizers(connector -> {
            // Set maximum request size (in bytes)
            connector.setMaxPostSize(maxRequestSizeMb * 1024 * 1024);
            
            // Set maximum number of parameters
            connector.setMaxParameterCount(100);
            
            // Additional connector properties can be set via server.tomcat properties
            // since some methods may not be available in newer versions
        });
    }
}
