package com.devs.simplicity.poke_go_friends.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web configuration for custom converters and formatters.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    private final TeamConverter teamConverter;
    
    public WebConfig(TeamConverter teamConverter) {
        this.teamConverter = teamConverter;
    }
    
    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(teamConverter);
    }
}
