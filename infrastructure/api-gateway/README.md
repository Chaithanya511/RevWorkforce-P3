# API Gateway

## Overview
The API Gateway is the **Single Entry Point** for all client requests. It handles routing, security, load balancing, and cross-cutting concerns for the RevWorkForce platform.

## Core Features & Methods

### 1. Dynamic Routing
Routes incoming requests to the appropriate microservice based on path patterns.
*   **Implementation**: `GatewayRoutesConfig.java` defines the route locator with predicates (e.g., `/api/users/**` -> `user-service`).

### 2. JWT Authentication
Secures all internal routes by verifying the JSON Web Token in the request header.
*   **Implementation**: `AuthenticationFilter.java` (Global Filter) checks the `Authorization` header and validates the token using shared secret keys.

### 3. Rate Limiting
Protects downstream services from traffic spikes and DoS attacks.
*   **Implementation**: `RateLimitFilter.java` implements a **Token Bucket Algorithm** to enforce a limit (e.g., 50 requests/sec with burst of 100).

### 4. Circuit Breaker (Resilience4j)
Prevents cascading failures by providing fallback logic if a microservice is unresponsive.
*   **Implementation**: Configured in `GatewayRoutesConfig.java` using the `circuitBreaker` filter with custom configuration IDs (e.g., `userServiceCB`).

### 5. CORS Configuration
Handles Cross-Origin Resource Sharing for the Angular frontend.
*   **Implementation**: `CorsConfig.java` allows specific origins, methods, and headers.

## Key Configuration
*   **Port**: `8080`
*   **Service Name**: `api-gateway`
