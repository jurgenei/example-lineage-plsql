# example-lineage-plsql

[![CI](https://github.com/jurgenei/example-lineage-plsql/actions/workflows/ci.yml/badge.svg)](https://github.com/jurgenei/example-lineage-plsql/actions/workflows/ci.yml)

Oracle PL/SQL lineage integration-test project. Spring Boot drives stored procedure execution; Liquibase XML defines schema/data/procedure; Testcontainers runs Oracle XE for local and CI parity.

## Stack

- Java 21
- Spring Boot 3.3
- Liquibase XML changelogs
- Testcontainers + Oracle XE (`gvenzl/oracle-xe:21-slim-faststart`)
- Gradle

## Project layout

- `src/main/resources/db/changelog/` Liquibase XML source of truth
- `src/main/java/com/example/lineage/` test application code that calls `load_customer_lineage`
- `src/test/java/com/example/lineage/` integration tests validating lineage behavior
- `.github/workflows/ci.yml` CI pipeline running containerized tests

## Prerequisites

- Docker running locally
- Java 21

## Run tests locally

```bash
./gradlew test
```

Run single test class:

```bash
./gradlew test --tests com.example.lineage.LineageLoaderIntegrationTest
```

## Debug locally

Use IDE JUnit debug on `LineageLoaderIntegrationTest`. Oracle XE container starts automatically via Testcontainers. Breakpoints can be set in `LineageLoaderService` or assertions.

## CI behavior

GitHub Actions runs `./gradlew test` on push and pull request. Runner Docker daemon hosts Oracle XE container started by Testcontainers.

## See CI results

GitHub UI:

- Workflow page: `https://github.com/jurgenei/example-lineage-plsql/actions/workflows/ci.yml`
- All runs: `https://github.com/jurgenei/example-lineage-plsql/actions`

GitHub CLI:

```bash
gh run list --workflow ci.yml --limit 10
gh run watch
gh run view --log
```

## Best practices for extending lineage

1. Add new schema/data/procedure changes only through new Liquibase XML changeSets.
2. Add failing integration test for new lineage rule before implementation change.
3. Keep Oracle-specific SQL in changelog SQL blocks and assert outcomes from target tables.
4. Prefer deterministic seed data and explicit mappings for stable CI execution.
