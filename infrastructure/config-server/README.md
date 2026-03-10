# Config Server

## Overview
The Config Server provides a **Centralized Configuration Management** system for all microservices in the RevWorkForce application. It allows for managing application properties across different environments without requiring a rebuild of the services.

## Core Features
1.  **Centralized Management**: All microservice properties are stored in a Git or local filesystem repository (`config-repo-files`).
2.  **Externalized Configuration**: Changes to configuration can be reflected in services without restarting them (via `/actuator/refresh`).
3.  **Environment Specificity**: Supports profiles (dev, prod, test) for environment-specific settings.

## Key Configuration
*   **Port**: `8888`
*   **Storage Path**: `classpath:/config-repo-files`
*   **Method Implementation**:
    *   `@EnableConfigServer`: Activates the centralized configuration server logic in the main application class.

## Usage
Microservices connect to this server at start-up to fetch their `application.properties` or `application.yml` using their service name as the identifier.
