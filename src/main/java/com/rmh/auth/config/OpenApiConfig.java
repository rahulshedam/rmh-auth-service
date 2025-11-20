package com.rmh.auth.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"dev", "qa"})
public class OpenApiConfig {

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
}
