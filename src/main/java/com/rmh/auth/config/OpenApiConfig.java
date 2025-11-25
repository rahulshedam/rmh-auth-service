package com.rmh.auth.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI authServiceOpenAPI() {
        SecurityScheme bearerScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("JWT token obtained from /api/auth/login endpoint");

        return new OpenAPI()
                .components(new Components().addSecuritySchemes(BEARER_SCHEME, bearerScheme))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME));
    }

    @Bean
    public GroupedOpenApi allAPIs() {
        return GroupedOpenApi.builder()
                .group("all")
                .packagesToScan("com.rmh.auth.controller")
                .pathsToMatch("/**")
                .build();
    }

    @Bean
    public GroupedOpenApi authControllerAPIs() {
        return GroupedOpenApi.builder()
                .group("auth")
                .packagesToScan("com.rmh.auth.controller")
                .pathsToMatch("/api/auth/**")
                .build();
    }

    @Bean
    public GroupedOpenApi healthControllerAPIs() {
        return GroupedOpenApi.builder()
                .group("health")
                .packagesToScan("com.rmh.auth.controller")
                .pathsToMatch("/actuator/health")
                .build();
    }

    @Bean
    public GroupedOpenApi userControllerAPIs() {
        return GroupedOpenApi.builder()
                .group("users")
                .packagesToScan("com.rmh.auth.controller")
                .pathsToMatch("/api/users/**")
                .build();
    }
}
