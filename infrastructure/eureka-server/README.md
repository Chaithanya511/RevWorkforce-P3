# Eureka Server

## Overview
The Eureka Server acts as the **Service Registry** for the RevWorkForce microservices ecosystem. It provides service discovery, allowing microservices to find and communicate with each other dynamically without hardcoding IP addresses or ports.

## Core Features
1.  **Service Registration**: Each microservice registers itself with Eureka on startup.
2.  **Service Discovery**: Services query Eureka to find instances of other services.
3.  **High Availability**: Monitors the health of service instances via heartbeats and removes unhealthy ones.

## Key Configuration
*   **Port**: `8761`
*   **Default Properties**:
    *   `eureka.client.register-with-eureka`: `false` (Server doesn't register with itself)
    *   `eureka.client.fetch-registry`: `false`
*   **Method Implementation**:
    *   `@EnableEurekaServer`: Activates the Service Discovery server in the main application class.

## Dashboard
Once running, the Eureka status dashboard is available at: `http://localhost:8761`
