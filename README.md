# example-lineage-plsql

![Conformance](https://img.shields.io/badge/Conformance-Check--All%20Passing-brightgreen)
[![Build and Test](https://github.com/jurgenei/example-lineage-plsql/actions/workflows/ci.yml/badge.svg)](https://github.com/jurgenei/example-lineage-plsql/actions/workflows/ci.yml)
[![Coverage CI](https://github.com/jurgenei/example-lineage-plsql/actions/workflows/coverage.yml/badge.svg)](https://github.com/jurgenei/example-lineage-plsql/actions/workflows/coverage.yml)
[![CodeQL](https://github.com/jurgenei/example-lineage-plsql/actions/workflows/codeql.yml/badge.svg)](https://github.com/jurgenei/example-lineage-plsql/actions/workflows/codeql.yml)
[![Dependency Check](https://github.com/jurgenei/example-lineage-plsql/actions/workflows/dependency-check.yml/badge.svg)](https://github.com/jurgenei/example-lineage-plsql/actions/workflows/dependency-check.yml)
[![SpotBugs Security](https://github.com/jurgenei/example-lineage-plsql/actions/workflows/spotbugs-security.yml/badge.svg)](https://github.com/jurgenei/example-lineage-plsql/actions/workflows/spotbugs-security.yml)
[![Dependabot](https://img.shields.io/badge/dependabot-enabled-025E8C?logo=dependabot)](https://github.com/jurgenei/example-lineage-plsql/security/dependabot)
[![Coverage](https://codecov.io/gh/jurgenei/example-lineage-plsql/graph/badge.svg)](https://codecov.io/gh/jurgenei/example-lineage-plsql)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/java-21+-green.svg)](https://www.oracle.com/java/)
[![Gradle](https://img.shields.io/badge/gradle-8+-blue.svg)](https://gradle.org/)

Architecture-driven lineage reference project. Repository grows as multi-project Gradle build: PL/SQL assets -> lineage artifacts -> graph verification -> end-to-end report.

## Stack

- Java 21
- Spring Boot 3.3 (application + knowledge-graph client)
- Liquibase XML changelogs (Oracle schema/procedure model)
- Testcontainers + Oracle XE (`gvenzl/oracle-xe:21-slim-faststart`)
- Testcontainers + Neo4j (`neo4j:5.23.0`)
- Gradle

## Multi-project layout

```text
example-lineage-plsql
├── 1-architecture/ # architecture intent exports
├── 2-application/  # application code + PL/SQL assets under analysis
│   ├── src/main/... # Spring Boot + Liquibase runtime
│   └── src/test/... # bootstrap/contract/e2e contract tests
├── 3-lineage/      # discovered lineage artifacts
├── 4-knowledge-graph/ # Spring Boot Neo4j client + graph verification/report
└── .github/workflows/ci.yml
```

## Prerequisites

- Docker running locally
- Java 21
- Archi runtime (for `:architecture:assembleArchitectureModel`)

## TDD growth progression

1. Structure bootstrap tests (`GrowthStructureBootstrapTest`)
2. Cross-module contract tests (`GrowthCrossModuleContractTest`)
3. End-to-end graph verification contract tests (`GrowthEndToEndGraphVerificationContractTest`)
4. Oracle integration test (`LineageLoaderIntegrationTest`)

## Root orchestration lifecycle

```bash
./gradlew assembleArchitecture
./gradlew generateLineage
./gradlew startKnowledgeGraph
./gradlew loadKnowledgeGraph
./gradlew verifyKnowledgeGraph
./gradlew endToEndTest
```

Generated root reports:

- `build/reports/architecture-report.html`
- `build/reports/lineage-report.html`
- `build/reports/knowledge-graph-start-report.html`
- `build/reports/graph-load-report.html`
- `build/reports/graph-report.html`
- `build/reports/end-to-end-report.html`

Generated knowledge-graph reports:

- `4-knowledge-graph/build/graph-report/container-start-report.json`
- `4-knowledge-graph/build/graph-report/load-report.json`
- `4-knowledge-graph/build/graph-report/graph-dump.json`
- `4-knowledge-graph/build/graph-report/verification-report.json`

## Local commands

Run staged growth tests:

```bash
./gradlew :application:test --tests name.jurgenei.example.lineage.GrowthStructureBootstrapTest
./gradlew :application:test --tests name.jurgenei.example.lineage.GrowthCrossModuleContractTest
./gradlew :application:test --tests name.jurgenei.example.lineage.GrowthEndToEndGraphVerificationContractTest
```

Run Oracle integration test:

```bash
./gradlew :application:test --tests name.jurgenei.example.lineage.LineageLoaderIntegrationTest
```

Run full local flow:

```bash
./gradlew verifyKnowledgeGraph endToEndTest
```

Run knowledge-graph scripts (Gradle tasks are source-of-truth):

```bash
bash 4-knowledge-graph/scripts/load-stub-graph.sh
bash 4-knowledge-graph/scripts/dump-stub-graph.sh
bash 4-knowledge-graph/scripts/verify-knowledge-graph.sh
```

Run architecture export from ArchiMate model (`1-architecture/src/main/resources/archimate/test-lineage.archimate`):

```bash
./gradlew :architecture:assembleArchitectureModel
```

This task uses real Archi runtime (not stub). Set `ARCHI_HOME` when Archi is not installed in default path.
It generates `build/archi-export/test-lineage.export.xml`, transforms it with `src/main/resources/xsl/archi2model.xsl`, and writes `build/archi-export/test-lineage.xml` for downstream phases.

## CI workflow stages

`ci.yml` runs four gated stages:

1. `structure-and-contract-tests`
2. `lineage-tests`
3. `graph-tests`
4. `e2e-tests`

Each stage uploads artifacts for inspection; graph stage also executes knowledge-graph load/dump script wrappers.

Coverage pipeline:

- `coverage.yml` runs `:application:test` + `:application:jacocoTestReport`, uploads JaCoCo reports, and publishes coverage to Codecov.

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
