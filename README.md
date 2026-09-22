# example-lineage-plsql

[![CI](https://github.com/jurgenei/example-lineage-plsql/actions/workflows/ci.yml/badge.svg)](https://github.com/jurgenei/example-lineage-plsql/actions/workflows/ci.yml)

Architecture-driven lineage reference project. Repository grows as multi-project Gradle build: PL/SQL assets -> lineage artifacts -> graph verification -> end-to-end report.

## Stack

- Java 21
- Spring Boot 3.3 (integration test application)
- Liquibase XML changelogs (Oracle schema/procedure model)
- Testcontainers + Oracle XE (`gvenzl/oracle-xe:21-slim-faststart`)
- Gradle

## Multi-project layout

```text
example-lineage-plsql
├── application/  # PL/SQL assets under analysis
├── archimate/    # architecture intent exports
├── lineage/      # discovered lineage artifacts
├── neo4j/        # graph verification logic/report
├── src/main/...  # Spring Boot test application
├── src/test/...  # bootstrap/contract/e2e contract tests
└── .github/workflows/ci.yml
```

## Prerequisites

- Docker running locally
- Java 21

## TDD growth progression

1. Structure bootstrap tests (`GrowthStructureBootstrapTest`)
2. Cross-module contract tests (`GrowthCrossModuleContractTest`)
3. End-to-end graph verification contract tests (`GrowthEndToEndGraphVerificationContractTest`)
4. Oracle integration test (`LineageLoaderIntegrationTest`)

## Root orchestration lifecycle

```bash
./gradlew assembleArchitecture
./gradlew generateLineage
./gradlew startNeo4j
./gradlew loadKnowledgeGraph
./gradlew verifyKnowledgeGraph
./gradlew endToEndTest
```

Generated root reports:

- `build/reports/architecture-report.html`
- `build/reports/lineage-report.html`
- `build/reports/neo4j-start-report.html`
- `build/reports/graph-load-report.html`
- `build/reports/graph-report.html`
- `build/reports/end-to-end-report.html`

## Local commands

Run staged growth tests:

```bash
./gradlew test --tests com.example.lineage.GrowthStructureBootstrapTest
./gradlew test --tests com.example.lineage.GrowthCrossModuleContractTest
./gradlew test --tests com.example.lineage.GrowthEndToEndGraphVerificationContractTest
```

Run Oracle integration test:

```bash
./gradlew test --tests com.example.lineage.LineageLoaderIntegrationTest
```

Run full local flow:

```bash
./gradlew verifyKnowledgeGraph endToEndTest
```

## CI workflow stages

`ci.yml` runs four gated stages:

1. `structure-and-contract-tests`
2. `lineage-tests`
3. `graph-tests`
4. `e2e-tests`

Each stage uploads artifacts; next stage downloads prior stage artifacts.

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

## Best practices

1. Add DB changes through Liquibase XML only.
2. Add failing test first, then wire task/build logic to green.
3. Keep module contracts explicit (artifacts + dependency chain).
4. Keep graph verification deterministic with explicit expected edges.
