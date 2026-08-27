# MYSHOPYCART - Development Environment

## Operating System

- Windows 11
- Docker Desktop
- WSL 2
- Ubuntu

## Development Tools

| Tool | Status |
|---|---|
| Java 21 | Installed |
| Maven | Installed |
| Git | Installed |
| SourceTree | Installed |
| Docker Desktop | Installed |
| Postman | Installed |

## Docker

Docker Desktop is used for local infrastructure.

- Docker version: 29.7.2
- Docker Compose version: v5.4.0
- Docker hello-world container: Successfully verified

## PostgreSQL

PostgreSQL is planned to run through Docker.

- Docker image: postgres:15-alpine
- Container: myshopycart-postgres
- Port: 5432
- Verification: SELECT 1;
- Result: 1
- Current state: Stopped

## MongoDB

MongoDB is planned to run through Docker.

- Docker image: mongo:8
- Container: myshopycart-mongodb
- Port: 27017
- Verification: db.runCommand({ ping: 1 })
- Result: { ok: 1 }
- Current state: Stopped

## Redis

Redis is planned to run through Docker.

- Docker image: redis:7-alpine
- Container: myshopycart-redis
- Port: 6379
- Verification: redis-cli ping
- Result: PONG
- Current state: Stopped

## Postman

Postman is installed and signed in.

- Collection: MYSHOPYCART
- Collection status: Empty
- API requests: Will be added beginning with Phase 2

## Resource Considerations

The development laptop has limited RAM.

PostgreSQL, MongoDB, and Redis are not kept running simultaneously during local development.

Each infrastructure component is started and verified individually when required.

## Repository

- Local repository: C:\Projects\myshopycart
- GitHub repository: Sumukhasumu/myshopycart
- Git branch: main

## Current Phase

Phase 1 - Development Environment & Repository Setup

No application or service implementation has been created yet.