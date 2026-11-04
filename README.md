# MYSHOPYCART

MYSHOPYCART is a learning-focused e-commerce marketplace project designed to demonstrate production-style Java backend development, microservices architecture, distributed systems, API design, security, messaging, observability, Docker, Kubernetes, and cloud deployment.

## Project Status

## Project Status

**Current Status:** Core Development Completed

MyShopyCart is a developed full-stack e-commerce microservices project. The core backend services, platform components, supporting infrastructure configurations, and frontend applications have been implemented.

### Implementation Overview

- **7 Business Microservices:** Auth, Product, Inventory, Cart, Order, Payment and Notification.
- **3 Platform Services:** API Gateway, Eureka Discovery Server and Config Server.
- **Supporting Infrastructure:** Kafka, Redis, RabbitMQ and MinIO.
- **Frontend Applications:** React + Vite customer storefront and Angular + TypeScript Architecture Explorer.
- **Backend Technologies:** Java 21, Spring Boot 3.3, Spring Cloud, REST APIs and database integrations.

### Project Deliverables

- Modular microservices-based e-commerce architecture.
- Service discovery and centralized configuration.
- API Gateway-based request routing.
- Authentication and authorization implementation.
- Event-driven communication infrastructure.
- Customer-facing storefront demonstration.
- Interactive architecture documentation and exploration.

**Development Note:** The project has been developed locally. Individual services and infrastructure components can be started when required. The frontend demonstrations are separately deployed and publicly accessible.

**Project Status:** Core implementation completed; further enhancements, testing and deployment activities can be continued.

## Project Goals

The project will demonstrate:

- Java 21 backend development
- Spring Boot microservices
- REST API design
- Authentication and authorization
- PostgreSQL, MongoDB, and Redis
- Kafka and RabbitMQ
- API Gateway and service discovery
- Distributed transactions and Saga patterns
- Docker and Kubernetes
- Automated testing
- Observability and tracing
- CI/CD
- AWS deployment

## Planned Services

The planned backend services include:

- auth-service
- product-service
- inventory-service
- cart-service
- order-service
- payment-service
- notification-service
- api-gateway
- discovery-server
- config-server

## Repository Structure

```text
myshopycart/
|-- services/
|-- infrastructure/
|-- frontend/
|-- docs/
|   |-- architecture/
|   |-- api/
|   |-- database/
|   |-- adr/
|   `-- interview-notes/
|-- .github/
|   `-- workflows/
|-- README.md
|-- ENVIRONMENT.md
`-- .gitignore
