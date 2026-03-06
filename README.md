# RevWorkForce – Microservices HRM Platform

RevWorkForce is a **microservices-based Human Resource Management (HRM) platform** designed to streamline employee management, leave tracking, and performance reviews.

The system is built using **Spring Boot, Spring Cloud, and Kubernetes**, and follows **domain-driven microservice architecture** with centralized configuration, service discovery, API gateway routing, and observability.

---

# Architecture Overview

The monolithic HRM system has been refactored into independent domain-driven microservices.

```
RevWorkForce-P3
│
├── infrastructure
│   ├── config-server
│   ├── eureka-server
│   └── api-gateway
│
├── services
│   ├── user-service
│   ├── leave-service
│   ├── performance-service
│   ├── employee-management-service
│   ├── notification-service
│   └── reporting-service
│
└── kubernetes (deployment manifests)
```

Each service maintains its **own database schema, repository layer, and lifecycle**, enabling independent development and deployment.

---

# Core Infrastructure Components

## Config Server

Centralized configuration management for all services.

* Externalized service configurations
* Environment-specific properties
* Git-based configuration repository

## Eureka Server

Service discovery and registration.

* Microservices automatically register with Eureka
* Enables dynamic service communication

## API Gateway

Single entry point for all client requests.

Responsibilities:

* Routing to microservices
* JWT authentication validation
* Rate limiting
* Request logging
* Circuit breaker support

---

# Microservices

## User Service

Handles authentication, authorization, and employee directory.

Features:

* User authentication
* Role-based access control (Employee / Manager / Admin)
* Profile management
* Employee directory
* Reporting hierarchy

Key Components:

* AuthController
* UserManagementController
* UserRepository
* AuthService
* UserManagementService

---

## Leave Service

Manages leave workflows and balances.

Features:

* Leave application
* Leave approval / rejection
* Leave balance tracking
* Holiday calendar
* Leave reports

Key Components:

* LeaveController
* LeaveService
* LeaveRepository
* LeaveBalanceRepository

---

## Performance Service

Handles employee performance reviews and goal tracking.

Features:

* Self performance reviews
* Goal creation and tracking
* Manager feedback
* Performance rating system

Key Components:

* PerformanceController
* PerformanceService
* GoalRepository
* ReviewRepository

---

## Employee Management Service

Manages employee lifecycle and organizational structure.

Features:

* Employee onboarding
* Department management
* Designation management
* Manager assignment
* Company announcements

Key Components:

* EmployeeController
* DepartmentController
* EmployeeRepository
* DepartmentRepository

---

## Notification Service

Handles real-time in-app notifications.

Features:

* Leave approval notifications
* Performance feedback notifications
* Announcement notifications
* Low leave balance alerts

Key Components:

* NotificationController
* NotificationService
* NotificationRepository

---

## Reporting Service

Provides HR analytics and dashboards.

Features:

* HR dashboard metrics
* Leave utilization reports
* Employee reports
* Organizational insights

This service aggregates data from other services using **OpenFeign clients**.

---

# Inter-Service Communication

Services communicate using **Spring Cloud OpenFeign clients**.

Example:

```
Reporting Service → User Service
Reporting Service → Leave Service
Reporting Service → Performance Service
```

This enables **loosely coupled service communication**.

---

# Technology Stack

### Backend

* Java 17
* Spring Boot
* Spring Cloud
* Spring Security
* Spring Data JPA
* OpenFeign

### Microservices Infrastructure

* Spring Cloud Gateway
* Eureka Service Discovery
* Config Server

### Databases

* MySQL (separate schema per service)

### Observability

* ELK Stack (Centralized logging)
* Prometheus (metrics collection)
* Grafana (monitoring dashboards)
* Distributed tracing

### Containerization & Orchestration

* Docker
* Kubernetes (Minikube)

---

# Deployment Architecture

Each microservice is containerized and deployed using Kubernetes.

Components include:

* Deployments
* Services
* ConfigMaps
* Secrets
* Horizontal Pod Autoscaler
* Ingress Controller

This enables:

* High availability
* Auto scaling
* Self-healing containers
* Service discovery within the cluster

---

# Key Features

* Domain-driven microservices architecture
* Role-based access control
* Leave management workflow
* Performance review management
* Employee directory
* Real-time notifications
* HR analytics dashboards
* Centralized configuration
* Service discovery
* API gateway routing
* Observability with monitoring and logging
* Kubernetes orchestration

---

# Development Workflow

1. Clone the repository
2. Start infrastructure services
3. Run microservices
4. Access APIs through API Gateway

Example order:

```
1. Config Server
2. Eureka Server
3. API Gateway
4. Microservices
```

---

# Future Enhancements

* Event-driven communication with Kafka
* Advanced analytics dashboards
* Email and push notification support
* Multi-tenant HR management
* Cloud deployment (AWS / GCP)

---

# Contributors

RevWorkForce Team

* Security & Infrastructure – API Gateway, Config Server, Eureka
* User Management Module
* Leave Management Module
* Performance Management Module
* Notification Module
* Reporting Module

---

# License

This project is for educational and demonstration purposes.
