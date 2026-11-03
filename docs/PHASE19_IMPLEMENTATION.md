## 7. Continuous Integration

A GitHub Actions workflow exists at:

`.github/workflows/ci.yml`

The workflow defines:
- Frontend dependency installation, tests, linting, and production build.
- A Maven verification matrix for all ten backend services.
- A dedicated MinIO startup step for the Order Service integration tests, using a pinned MinIO-compatible image.

The Compose files passed configuration validation. Text checks confirmed that the expected frontend and backend jobs, image reference, and Maven verification command are present in the workflow.

However, these checks do not constitute full YAML or GitHub Actions validation. The workflow has not been executed on GitHub, and the Order Service integration tests have not been successfully verified in the final CI environment.

The local Maven-container integration attempt encountered Testcontainers networking failures involving PostgreSQL and Ryuk. A separate local MinIO health check returned HTTP 200, but this does not establish that invoice-storage integration tests pass.

**Status:** CI workflow prepared; full CI execution and Order Service integration verification remain pending. Do not report CI as passing until actual execution results are available.