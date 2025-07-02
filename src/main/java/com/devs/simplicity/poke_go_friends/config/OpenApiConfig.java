package com.devs.simplicity.poke_go_friends.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI configuration for the Pokemon Go Friend Code Sharing API.
 * Configures Swagger UI documentation with API information, servers, and contact details.
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Pokemon Go Friend Code Sharing API")
                        .description("REST API for sharing and managing Pokemon Go friend codes. " +
                                   "This API allows trainers to submit their friend codes, search for other trainers, " +
                                   "and manage their submissions for easier friend connections in Pokemon Go.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Pokemon Go Friends Team")
                                .email("support@pokegofriends.dev")
                                .url("https://github.com/pokemon_go_code_sharing_backend"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Local development server"),
                        new Server()
                                .url("https://api.pokegofriends.dev")
                                .description("Production server")
                ));
    }
}
