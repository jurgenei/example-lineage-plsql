# 4-Knowledge-Graph Evolution Plan

## Objective

Transform `4-knowledge-graph` from a lineage visualization component into a lightweight metadata platform that:

- Stores technical lineage in Neo4j.
- Emulates a subset of Collibra ingestion APIs.
- Supports batch-oriented metadata ingestion using Spring Batch.
- Supports load testing and contract validation.
- Provides governance-style catalog capabilities without requiring a Collibra installation.
- Keeps lineage and catalog information in a single graph.

---

# Architectural Principles

## Principle 1: Single Source of Truth

Use one Neo4j database.

Do not create:

- separate lineage database
- separate catalog database
- synchronization layer between two graphs

Everything is stored in the same graph.

## Principle 2: Metadata API Instead of Catalog API

Expose one metadata API.

Both lineage loaders and catalog loaders use the same API.

```text
Parser
  │
  ▼
Metadata API
  ▲
  │
Catalog Loader
  │
  ▼
Neo4j
```

## Principle 3: Lineage and Catalog are Different Views

Lineage:

```text
Procedure -> Table
Table -> Column
Job -> Procedure
```

Catalog:

```text
Domain -> Table
BusinessTerm -> Table
Owner -> Table
```

Both are represented in the same graph.

---

# Target Modules

```text
4-knowledge-graph
│
├── knowledge-graph-api
├── knowledge-graph-domain
├── knowledge-graph-neo4j
├── knowledge-graph-batch
├── knowledge-graph-loadtest
└── knowledge-graph-testsupport
```

---

# Domain Model

## Asset

Base concept.

```text
Asset
├── Table
├── Column
├── Procedure
├── Job
├── Domain
├── BusinessTerm
├── Community
└── Person
```

Required properties:

```text
assetId
assetType
name
sourceSystem
createdAt
updatedAt
```

---

# Relationship Model

Technical lineage:

```text
READS
WRITES
CALLS
CONTAINS
```

Catalog relationships:

```text
BELONGS_TO
OWNED_BY
DESCRIBES
PART_OF
```

Mapping relationships:

```text
SAME_AS
```

---

# Neo4j Constraints

Create unique constraints.

Example:

```cypher
CREATE CONSTRAINT asset_id_unique
IF NOT EXISTS
FOR (a:Asset)
REQUIRE a.assetId IS UNIQUE;
```

Create indexes on:

```text
assetId
name
assetType
sourceSystem
```

---

# Collibra Ingestion API Emulator

Purpose:

Provide endpoints that look sufficiently similar to a catalog ingestion interface.

## Endpoints

### Assets

```http
POST /api/assets
GET  /api/assets/{id}
GET  /api/assets
```

### Relationships

```http
POST /api/relationships
GET  /api/relationships
```

### Lineage

```http
POST /api/lineage
```

### Validation

```http
POST /api/validation
```

---

# Validation Rules

Reject:

```text
unknown asset type
missing domain
duplicate asset
invalid relationship
empty names
```

Return structured responses.

```json
{
  "status": "VALIDATION_ERROR",
  "message": "Asset already exists"
}
```

---

# Spring Batch Integration

Purpose:

Enable large metadata loads.

## Jobs

### AssetImportJob

Reads:

```text
CSV
JSON
```

Writes:

```text
Neo4j Asset Nodes
```

### RelationshipImportJob

Creates graph relationships.

### ValidationJob

Runs graph quality checks.

### MetricsJob

Produces summary statistics.

---

# Graph Quality Metrics

Produce metrics such as:

```text
Total Assets
Total Relationships
Catalog Coverage
Orphan Assets
Missing Owners
Domain Coverage
Lineage Coverage
Duplicate Detection
```

---

# Cypher Governance Checks

## Assets missing owner

```cypher
MATCH (a:Asset)
WHERE NOT EXISTS {
 MATCH (a)-[:OWNED_BY]->(:Person)
}
RETURN a
```

## Catalog assets not represented in lineage

```cypher
MATCH (a:Table)
WHERE NOT EXISTS {
 MATCH (:Procedure)-[:READS|WRITES]->(a)
}
RETURN a
```

## Duplicate names

```cypher
MATCH (a:Asset)
WITH a.name AS name,count(*) AS cnt
WHERE cnt > 1
RETURN name,cnt
```

---

# Load Testing

Use k6.

Scenarios:

## Small

```text
1,000 assets
10,000 relationships
```

## Medium

```text
10,000 assets
100,000 relationships
```

## Large

```text
100,000 assets
1,000,000 relationships
```

Measure:

```text
Throughput
Response time
Neo4j write latency
Validation duration
```

---

# Testcontainers Strategy

Keep Testcontainers as primary runtime.

```java
Neo4jContainer
```

Avoid Docker Compose during automated tests.

Compose may be added purely for demonstrations.

---

# Demonstration Flow

Step 1

Load Oracle-derived lineage.

Step 2

Create catalog domains.

Step 3

Create business terms.

Step 4

Link assets.

Step 5

Run governance checks.

Step 6

Run load tests.

Step 7

Generate metrics.

---

# Future Extensions

- OpenMetadata adapter
- DataHub adapter
- ArchiMate export
- GraphQL API
- LLM-assisted metadata enrichment
- Catalog coverage dashboards
- Drift detection between observed and governed reality

---

# Definition of Done

- Single Neo4j database.
- Spring Boot metadata API.
- Spring Batch ingestion.
- Testcontainers integration.
- Neo4j constraints and indexes.
- Catalog ingestion API emulator.
- Governance validation queries.
- k6 load tests.
- CI execution.
- Documentation and examples.
