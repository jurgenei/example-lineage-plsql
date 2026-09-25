# 4-knowledge-graph

Knowledge-graph module now exposes two complementary capabilities:

1. Existing deterministic lineage smoke flow (Testcontainers + Neo4j).
2. Collibra-emulator style metadata ingestion API backed by same Neo4j graph.

## API surface

```text
POST /api/assets
GET  /api/assets/{id}
GET  /api/assets
POST /api/relationships
GET  /api/relationships
POST /api/lineage
POST /api/validation
```

## Concept model

Single graph keeps both technical lineage and governance metadata.

- Technical view: `READS`, `WRITES`, `CALLS`, `CONTAINS`
- Catalog view: `BELONGS_TO`, `OWNED_BY`, `DESCRIBES`, `PART_OF`, `SAME_AS`

Asset type set:

```text
TABLE, COLUMN, PROCEDURE, JOB, DOMAIN, BUSINESS_TERM, COMMUNITY, PERSON
```

Validation rules include:

- unknown asset type
- empty name
- missing source system
- missing domain for domain-bound asset types
- invalid relationship definitions

## Fast local verification

Run module tests only:

```bash
./gradlew :knowledgeGraph:test
```

Run existing knowledge-graph flow tasks:

```bash
./gradlew :knowledgeGraph:startKnowledgeGraphContainer
./gradlew :knowledgeGraph:loadKnowledgeGraphStubData
./gradlew :knowledgeGraph:verifyKnowledgeGraphModel
```
