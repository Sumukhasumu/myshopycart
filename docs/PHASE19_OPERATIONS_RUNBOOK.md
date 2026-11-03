# MYSHOPYCART — Phase 19 Operations Runbook

## 1. Purpose

This runbook provides safe local commands for validating configuration, starting and checking MYSHOPYCART services, troubleshooting common issues, running tests, and stopping services during Phase 19.

The project uses Windows, CMD, Docker Desktop, Java 21, and a multi-module backend.

The development machine has limited memory, so start only the services required for the current task.

**Safety requirements:**

- Preserve existing Docker volumes and application data.
- Preserve all uncommitted and untracked project files.
- Do not commit or push changes until the entire MYSHOPYCART project is complete.
- Do not assume that a configured service has been tested successfully.
- Do not use destructive cleanup commands during routine verification.

## 2. Project Location and Environment

Project root:

`C:\Projects\myshopycart`

Frontend:

`C:\Projects\myshopycart\frontend`

Open the project root in CMD:

```cmd
cd /d C:\Projects\myshopycart
```

The backend contains separate Maven modules under the `services` directory.

Native Maven is not installed in the current development environment. Therefore, do not assume that the `mvn` command works directly in Windows CMD. Use the project's established Maven-in-Docker workflow when appropriate, while recognizing that the previous containerized Order Service test attempt encountered Testcontainers networking failures.

Do not repeat a known failing test approach without first addressing its underlying infrastructure limitation.

## 3. Validate Docker Compose Configuration

Run these commands from the project root:

```cmd
cd /d C:\Projects\myshopycart
docker compose -f docker-compose.yml config --quiet
docker compose -f docker-compose.app.yml config --quiet
```

A successful command with exit code zero indicates that Docker Compose accepted the configuration.

This check does **not** prove that:

- All container images can be pulled.
- All containers start successfully.
- Applications become healthy.
- Services can communicate.
- Database migrations succeed.
- Unit or integration tests pass.

Record configuration validation separately from runtime and test results.

## 4. Inspect Containers Before Starting Services

Inspect existing containers first:

```cmd
docker ps -a
```

Inspect available images when necessary:

```cmd
docker images
```

Check the existing state before creating another container or attempting to bind a port.

For example, the dedicated Phase 19 local test container has been identified as:

`myshopycart-minio-phase19-test`

Its endpoint has previously returned HTTP 200 at the MinIO health URL. That historical result does not prove that invoice storage integration tests pass.

The main `myshopycart-minio` container was previously stopped. Do not start or stop either container without checking which one the current task needs.

## 5. Start Infrastructure When Required

Start the infrastructure defined by the root Compose file only when the current task needs it:

```cmd
cd /d C:\Projects\myshopycart
docker compose -f docker-compose.yml up -d
```

Check the resulting container state:

```cmd
docker ps -a
```

Review logs for a specific container only after confirming its actual name:

```cmd
docker logs --tail 100 <container-name>
```

Examples of previously referenced container names include:

- `myshopycart-postgres`
- `myshopycart-kafka`
- `myshopycart-rabbitmq`

These names are examples, not guarantees. Verify the actual container names using `docker ps -a` before running the log command.

Do not start every dependency merely because it exists in the Compose file. Start only what the current task requires.

## 6. Start Application Services When Required

Start the application services defined in the application Compose file only when needed:

```cmd
cd /d C:\Projects\myshopycart
docker compose -f docker-compose.app.yml up -d
```

To start one service, first confirm its exact Compose service name:

```cmd
docker compose -f docker-compose.app.yml config --services
```

Then start the selected service using its confirmed name:

```cmd
docker compose -f docker-compose.app.yml up -d <service-name>
```

Check the container state:

```cmd
docker ps -a
```

A successful `up -d` command does not necessarily mean the application is ready. Check logs and the appropriate health endpoint before treating the service as operational.

## 7. Health Checks

### 7.1 Notification Service

Previously verified endpoint:

```cmd
curl.exe -i http://localhost:8087/actuator/health
```

Expected healthy response body:

```json
{"status":"UP"}
```

This is a historical verification result. Run the command again when current runtime evidence is required.

### 7.2 Eureka Discovery Server

Check the Eureka home page:

```cmd
curl.exe -i http://localhost:8761/
```

Inspect the service registry:

```cmd
curl.exe -s -H "Accept: application/json" http://localhost:8761/eureka/apps
```

Confirm that the expected services appear in the registry.

Registration may take time after startup. A service missing from the registry requires further investigation rather than an assumption that registration will eventually succeed.

### 7.3 Config Server

Check the Notification Service configuration endpoint:

```cmd
curl.exe -i http://localhost:8888/notification-service/default
```

A successful response should return the configuration environment for Notification Service.

A successful Config Server response confirms that the endpoint responded. It does not prove that every application has loaded the correct configuration or started successfully.

### 7.4 MinIO-compatible storage

When the required test storage container is running, check its health endpoint:

```cmd
curl.exe -i http://localhost:9100/minio/health/live
```

An HTTP 200 response indicates that the health endpoint responded. It does not establish that invoice uploads, downloads, bucket operations, or temporary URL generation work correctly.

Do not start a second container on port 9100 while the existing test container is using that port.

## 8. Troubleshooting

### 8.1 View application logs

First identify the actual container name:

```cmd
docker ps -a
```

Then inspect the relevant container:

```cmd
docker logs --tail 150 <container-name>
```

To follow logs continuously:

```cmd
docker logs -f <container-name>
```

Press `Ctrl + C` to stop following logs. This does not stop the container.

### 8.2 Eureka registration failures

1. Confirm that Discovery Server is running.
2. Check the application's Eureka URL.
3. When the application runs inside Docker, verify that its Eureka URL uses the appropriate Docker service hostname rather than `localhost`.
4. Review the application startup logs.
5. Correct the configuration before recreating the affected service.

For Notification Service, the previously verified Eureka URL was:

`http://discovery-server:8761/eureka/`

Confirm that the current Compose configuration still uses this service name before relying on the URL.

### 8.3 RabbitMQ connection failures

1. Confirm that the RabbitMQ container is running.
2. Check the broker hostname used by the application.
3. When both containers are on the same Compose network, verify that the hostname is the configured RabbitMQ service name, previously identified as `rabbitmq`.
4. Confirm that credentials match the existing broker configuration.
5. Review application and broker logs.

Do not reset credentials, delete broker data, or remove the RabbitMQ volume as a routine troubleshooting step.

### 8.4 Port conflicts

Inspect existing containers:

```cmd
docker ps -a
```

If a port is already allocated, identify which container is using it before attempting to start another service.

Do not remove the existing container merely to free the port. Determine whether it belongs to an active task or contains data that must be preserved.

### 8.5 Environment variables and credentials

Inspect only the relevant configuration values.

Do not paste or share production passwords, JWT signing secrets, SMTP passwords, access keys, or other sensitive values in logs or chat.

Use test-only credentials for integration tests.

## 9. Frontend Verification

Run the frontend checks from its directory:

```cmd
cd /d C:\Projects\myshopycart\frontend
npm test -- --run
npm run lint
npm run build
```

Check the exit code and output for each command. A command that fails must be investigated before the relevant check is marked as passing.

Previous local verification reported:

- 36 tests passing across 6 test files.
- ESLint passing.
- Production build passing.

These are historical results. Run the commands again after the final Phase 19 source changes to establish the final result.

Return to the project root when finished:

```cmd
cd /d C:\Projects\myshopycart
```

## 10. Backend Verification

Backend verification uses Maven and is organized into separate service modules.

The native `mvn` command is not installed in the current Windows environment. Do not run native Maven commands expecting them to work unless the environment has changed.

The Order Service module is located at:

`C:\Projects\myshopycart\services\order-service\pom.xml`

The previously attempted Maven-in-Docker approach encountered Testcontainers networking failures involving PostgreSQL/Ryuk. Do not treat that attempt as a successful integration test run.

Before retrying backend verification:

1. Confirm the intended test scope.
2. Confirm the Maven execution environment.
3. Confirm that required containers and networks are accessible from that environment.
4. Use test-only configuration and credentials.
5. Capture the final Maven exit code and test summary.
6. Distinguish infrastructure failures from actual application test failures.

For a service whose integration-test prerequisites are available, run verification using the project's established Maven workflow and that service's POM.

Do not report a module as passing without a successful test summary for the relevant source state.

## 11. Stop Services Safely

Stop services only when they are no longer needed.

### 11.1 Stop Compose-managed application services

```cmd
cd /d C:\Projects\myshopycart
docker compose -f docker-compose.app.yml stop
```

This stops the services managed by that Compose configuration. It does not intentionally delete persistent volumes.

### 11.2 Stop Compose-managed infrastructure

```cmd
cd /d C:\Projects\myshopycart
docker compose -f docker-compose.yml stop
```

Use this only when the infrastructure is no longer needed by another task.

### 11.3 Stop one container

For a container managed outside the relevant Compose configuration, first confirm its identity and current state:

```cmd
docker ps -a
```

If it is safe to stop, use its confirmed name:

```cmd
docker stop <container-name>
```

Do not stop containers that another active task needs.

Keep Docker Desktop open unless there is a specific reason to close it.

### 11.4 Prohibited routine cleanup

Do not use these commands as routine cleanup:

- `docker compose down -v`
- `docker volume prune`
- `docker system prune`
- `docker compose ... --remove-orphans`

These commands can remove resources or data that must be preserved.

Stopping a container is different from deleting a container or its volumes. Prefer stopping over removal when the task only requires services to be inactive.

## 12. Git Safety

The project must not be committed or pushed until all MYSHOPYCART development is complete.

Preserve existing modified and untracked files.

Do not use destructive Git cleanup or reset commands to make the working tree appear clean.

Do not delete unexplained files, backups, or generated artifacts without explicit approval.

## 13. Phase 19 Verification Checklist

Update this checklist only when there is evidence for each item.

- [ ] Final frontend tests pass.
- [ ] Final frontend lint passes.
- [ ] Final frontend production build passes.
- [ ] Final backend verification results are recorded for all required modules.
- [ ] Order Service Testcontainers integration tests pass in a suitable environment.
- [x] Docker Compose configuration parsing passed previously for both Compose files.
- [ ] Required container images are verified for the intended tests.
- [ ] Required service health checks pass in the current environment.
- [ ] Notification Service Eureka registration is confirmed in the current environment.
- [ ] GitHub Actions workflow YAML is validated.
- [ ] GitHub Actions hosted execution succeeds when repository publication is authorized.
- [ ] Image publishing readiness is verified if included in the final Phase 19 scope.
- [ ] The four Phase 19 documentation files are reviewed and consistent.
- [ ] Final results and known limitations are documented.

The checked Compose item records a previous successful configuration validation. It does not imply that runtime verification is complete.

## 14. Known Boundaries and Completion Status

This runbook covers local Phase 19 operations.

It does not claim successful production deployment, registry publishing, or a successful hosted CI run.

The current known limitations include:

- The final Order Service Testcontainers integration result remains pending.
- The previous local Maven-in-Docker attempt encountered test infrastructure connectivity errors.
- The GitHub Actions workflow has not been confirmed successful on a hosted runner.
- Historical frontend and backend results must not be substituted for final verification of changed source.

Kubernetes and AWS deployment remain Phase 20 scope.

**Current status: Phase 19 verification in progress.**

Phase 19 should be marked complete only after the required tests, container checks, workflow verification, and documentation updates have been completed and their results recorded.
