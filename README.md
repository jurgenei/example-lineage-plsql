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

A runnable reference implementation demonstrating how lineage can be derived from implementation artifacts, verified through automated tests, and published as reusable lineage and graph-based knowledge.

The project provides an end-to-end example spanning:

- Architecture intent
- Oracle schema management with Liquibase
- PL/SQL transformations
- Lineage derivation
- Knowledge graph publication
- Automated verification
- End-to-end reporting

Rather than treating lineage as documentation, this repository treats lineage as a reproducible engineering artifact.

---

# Why This Repository Exists

Many lineage examples produce diagrams.

This repository demonstrates a complete and executable lineage lifecycle:

```mermaid
flowchart LR
    A[Architecture Intent]
    B[Implementation Reality]
    C[Derived Knowledge]
    D[Verifiable Lineage]
    A --> C
    B --> C
    C --> D
```

Every lineage assertion should:

- originate from evidence
- be reproducible
- be testable
- be traceable back to source artifacts

The repository is intended as:

- a learning resource
- a reference implementation
- a verification platform

---

# Relationship to the Jurgenei Initiative

This repository is the executable reference implementation.

Broader concepts such as:

- Near-Reality Lineage
- Continuous Architecture Reconciliation
- Extract → Derive → Resolve → Publish
- Canonical Enterprise Graphs
- Enterprise Knowledge Compilation

belong in the `jurgenei` repository.

This repository focuses on demonstrating those ideas through running software and verifiable results.

---

# What Gets Built

```mermaid
flowchart TB
    A[ArchiMate Model] --> B[Architecture Export]
    B --> C[Liquibase Deployment]
    C --> D[Oracle Schema]
    D --> E[PL/SQL Procedures]
    E --> F[Lineage Artifacts]
    F --> G[Neo4j Knowledge Graph]
    G --> H[Verification Reports]
```

---

# Learning Paths

## Reader

Understand the concepts, architecture, reports and generated outputs.

## Practitioner

Run the complete lifecycle and inspect generated lineage.

## Contributor

Extend architecture, database, lineage or graph capabilities while maintaining verification.

---

# Progressive Learning Levels

## Level 0 – Environment

Build the platform and start required services.

## Level 1 – Architecture Intent

Export architecture models and establish intended relationships.

## Level 2 – Database Model

Deploy Liquibase definitions and create Oracle objects.

## Level 3 – Transformation Logic

Execute PL/SQL transformations.

## Level 4 – Lineage Derivation

Generate lineage from deployed artifacts.

## Level 5 – Knowledge Graph Publication

Load derived information into Neo4j.

## Level 6 – Verification

Validate expected relationships and lineage paths.

---

# Technology Stack

- Java 21
- Spring Boot 3.3
- Liquibase XML changelogs
- Oracle XE via Testcontainers
- Neo4j via Testcontainers
- Gradle

---

# Repository Structure

```mermaid
flowchart LR
    R[example-lineage-plsql]
    R --> A[1-architecture]
    A --> A1[Architecture intent exports]
    R --> B[2-application]
    B --> B1[Spring Boot runtime]
    B --> B2[Liquibase assets]
    B --> B3[PL/SQL assets under analysis]
    R --> C[3-lineage]
    C --> C1[Generated lineage artifacts]
    R --> D[4-knowledge-graph]
    D --> D1[Neo4j publication and verification]
    R --> E[.github/workflows]
```

---

# Prerequisites

- Docker
- Java 21
- Archi Runtime (for architecture export)

---

# Quick Start

Run the complete end-to-end lifecycle:

```bash
./gradlew endToEndTest
```

Or execute stages individually:

```bash
./gradlew assembleArchitecture
./gradlew generateLineage
./gradlew startKnowledgeGraph
./gradlew loadKnowledgeGraph
./gradlew verifyKnowledgeGraph
./gradlew endToEndTest
```

Expected outcome:

```text
✓ Architecture exported
✓ Schema deployed
✓ Procedures created
✓ Lineage generated
✓ Graph loaded
✓ Verification completed
```

---

# Verification Strategy

Lineage is treated as an engineering discipline.

Verification is performed through:

- Structure bootstrap tests
- Cross-module contract tests
- End-to-end graph verification tests
- Oracle integration tests

Build success demonstrates that derived outputs satisfy expected relationships.

---

# TDD Growth Progression

- GrowthStructureBootstrapTest
- GrowthCrossModuleContractTest
- GrowthEndToEndGraphVerificationContractTest
- LineageLoaderIntegrationTest

The project grows incrementally from structure validation toward full end-to-end lineage verification.

---

# Generated Reports

## Root Reports

```mermaid
flowchart LR
    R[build/reports]
    R --> R1[architecture-report.html]
    R --> R2[lineage-report.html]
    R --> R3[knowledge-graph-start-report.html]
    R --> R4[graph-load-report.html]
    R --> R5[graph-report.html]
    R --> R6[end-to-end-report.html]
```

## Knowledge Graph Reports

```mermaid
flowchart LR
    G[4-knowledge-graph/build/graph-report]
    G --> G1[container-start-report.json]
    G --> G2[load-report.json]
    G --> G3[graph-dump.json]
    G --> G4[verification-report.json]
```

---

# CI/CD

The CI pipeline validates four gated stages:

- structure-and-contract-tests
- lineage-tests
- graph-tests
- e2e-tests

Coverage is generated through JaCoCo and published via Codecov.

---

# Local Development

Run staged growth tests:

```bash
./gradlew :application:test --tests name.jurgenei.example.lineage.GrowthStructureBootstrapTest
./gradlew :application:test --tests name.jurgenei.example.lineage.GrowthCrossModuleContractTest
./gradlew :application:test --tests name.jurgenei.example.lineage.GrowthEndToEndGraphVerificationContractTest
```

Run Oracle integration tests:

```bash
./gradlew :application:test --tests name.jurgenei.example.lineage.LineageLoaderIntegrationTest
```

Run graph verification:

```bash
./gradlew verifyKnowledgeGraph endToEndTest
```

---

# Design Principles

- Derive rather than manually maintain metadata.
- Keep lineage reproducible.
- Prefer evidence over documentation.
- Keep module contracts explicit.
- Verify graph structures deterministically.
- Treat architecture and lineage as continuously verifiable artifacts.

---

# Success Criteria

A successful build should prove that:

```mermaid
flowchart TD
    A[Architecture intent] --> B[Deployment artifacts]
    B --> C[Derived lineage]
    C --> D[Knowledge graph]
    D --> E[Expected relationships]
```

remain consistent and verifiable.

The goal is not merely to generate lineage.

The goal is to generate lineage that can be trusted.
