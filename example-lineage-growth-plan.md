# Example Lineage PLSQL - Growth Plan

## Objective

Evolve the current single-project repository into a multi-project Gradle build that demonstrates:

- Oracle PL/SQL lineage extraction
- ArchiMate integration
- Neo4j graph persistence and stitching
- End-to-end verification
- Single-command execution from the repository root

The repository should become both:

1. A reference implementation.
2. An educational journey demonstrating architecture-driven lineage.

---

# Target Repository Structure

```text
example-lineage-plsql
│
├── application
├── archimate
├── lineage
├── neo4j
│
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

---

# Subproject Responsibilities

## application

Represents the system under analysis.

Contains:

```text
Oracle DDL
Oracle sample data
Stored procedures
Packages
Views
Test fixtures
```

Example:

```text
application
└── src/main/plsql
    └── load_customer_lineage.sql
```

Output:

```text
PL/SQL artifacts consumed by lineage
```

No parsing.
No graph generation.
No lineage logic.

---

## archimate

Represents architectural intent.

Contains:

```text
System actors
Application components
Data objects
Relationships
```

Example:

```text
PreProcessor
    CALLS
LOAD_CUSTOMER_LINEAGE
```

Plugins:

```text
gradle-archi-plugin
gradle-xml-plugin
gradle-python-plugin
```

Outputs:

```text
test-lineage.export.xml
test-lineage.xml
test-lineage.xlsx
pdf/*.pdf
```

Purpose:

Provide application context that cannot yet be discovered automatically.

---

## lineage

Represents discovered technical reality.

Input:

```text
application project
```

Plugins:

```text
gradle-antlr-plsql-plugin
gradle-xml-plugin
```

Pipeline:

```text
PLSQL
  -> AST
  -> XML
  -> Flow Graph
  -> Table Lineage
  -> Cypher
```

Generated artifacts:

```text
asts/
flowgraphs/
lineage/
cypher/
reports/
```

Scope 1:

```text
Table lineage only
```

Out of scope:

```text
Column lineage
Expression lineage
Data quality lineage
```

---

## neo4j

Represents runtime knowledge graph validation.

Uses:

```text
Neo4j Container
Testcontainers
Cypher stitching
```

Consumes:

```text
Archimate artifacts
Lineage Cypher
```

Creates:

```text
Nodes
Relationships
Verification reports
```

Responsibilities:

### Load Graph

```text
Application Components
Stored Procedures
Tables
```

### Stitch Graph

```text
PreProcessor
    CALLS
LOAD_CUSTOMER_LINEAGE

LOAD_CUSTOMER_LINEAGE
    READS
RAW_CUSTOMER

LOAD_CUSTOMER_LINEAGE
    WRITES
VALID_CUSTOMER
```

### Verify Graph

Expected lineage:

```text
CUSTOMER_REFERENCE
    -> VALID_CUSTOMER

RAW_CUSTOMER
    -> VALID_CUSTOMER

VALID_CUSTOMER
    -> MERGED_CUSTOMER
```

---

# Top-Level Orchestration

The root project owns orchestration only.

It should not contain lineage logic.

Responsibilities:

```text
Build ordering
Dependency management
Integration testing
Reporting
Full-cycle execution
```

---

# Build Dependency Flow

```text
application
   |
   +----------------+
                    |
                    v
              archimate
                    |
application --------+
                    |
                    v
                lineage
                    |
                    v
                 neo4j
```

---

# Gradle Lifecycle

## assembleArchitecture

Produces:

```text
ArchiMate exports
```

---

## generateLineage

Produces:

```text
AST
Flow Graphs
Lineage
Cypher
```

---

## startNeo4j

Starts:

```text
Neo4j Testcontainer
```

---

## loadKnowledgeGraph

Loads:

```text
Architecture
Lineage
```

---

## verifyKnowledgeGraph

Runs:

```text
Cypher assertions
```

Verifications:

```text
Expected vertices exist
Expected relationships exist
Expected table lineage exists
```

---

## endToEndTest

Single integration scenario:

```text
1. Export ArchiMate
2. Parse PL/SQL
3. Generate AST
4. Generate Flow Graph
5. Generate Table Lineage
6. Generate Cypher
7. Start Neo4j
8. Load Graph
9. Stitch Graph
10. Execute Verification Queries
11. Produce Report
```

---

# Deliverables

Successful execution should generate:

```text
build/reports/architecture-report.html
build/reports/lineage-report.html
build/reports/graph-report.html
build/reports/end-to-end-report.html
```

---

# Phase 1 Success Criteria

The repository demonstrates:

```text
Application Component
        ->
Stored Procedure
        ->
Source Tables
        ->
Target Tables
```

With verification executed automatically through Neo4j.

---

# Future Phases

## Phase 2

```text
Column lineage
```

## Phase 3

```text
Java call discovery
```

## Phase 4

```text
Impact analysis
```

## Phase 5

```text
Near-reality enterprise model
```

Combining:

```text
Architecture
+ Source Code
+ Database Assets
+ Lineage
+ Knowledge Graph
```
