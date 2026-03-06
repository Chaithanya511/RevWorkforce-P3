package com.rev.api_gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutesConfig {

    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder) {

        return builder.routes()

                // USER SERVICE
                .route("user-service", r -> r
                        .path("/auth/**", "/users/**")
                        .filters(f -> f.circuitBreaker(c ->
                                c.setName("userServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/user")))
                        .uri("lb://USER-SERVICE"))

                // EMPLOYEE MANAGEMENT SERVICE
                .route("employee-service", r -> r
                        .path("/employees/**", "/departments/**")
                        .filters(f -> f.circuitBreaker(c ->
                                c.setName("employeeServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/employee")))
                        .uri("lb://EMPLOYEE-MANAGEMENT-SERVICE"))

                // LEAVE SERVICE
                .route("leave-service", r -> r
                        .path("/leave/**")
                        .filters(f -> f.circuitBreaker(c ->
                                c.setName("leaveServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/leave")))
                        .uri("lb://LEAVE-SERVICE"))

                // PERFORMANCE SERVICE
                .route("performance-service", r -> r
                        .path("/performance/**", "/goals/**")
                        .filters(f -> f.circuitBreaker(c ->
                                c.setName("performanceServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/performance")))
                        .uri("lb://PERFORMANCE-SERVICE"))

                // NOTIFICATION SERVICE
                .route("notification-service", r -> r
                        .path("/notifications/**")
                        .filters(f -> f.circuitBreaker(c ->
                                c.setName("notificationServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/notification")))
                        .uri("lb://NOTIFICATION-SERVICE"))

                // REPORTING SERVICE
                .route("reporting-service", r -> r
                        .path("/reports/**")
                        .filters(f -> f.circuitBreaker(c ->
                                c.setName("reportingServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/report")))
                        .uri("lb://REPORTING-SERVICE"))

                .build();
    }
}