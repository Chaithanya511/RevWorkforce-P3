package com.rev.api_gateway.config;

import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SecurityConfig {

    public static final List<String> PUBLIC_ROUTES = List.of(

            "/auth/login",
            "/auth/register",
            "/actuator",
            "/actuator/**"

    );

    public static boolean isPublicRoute(String path) {

        return PUBLIC_ROUTES
                .stream()
                .anyMatch(path::startsWith);
    }
}