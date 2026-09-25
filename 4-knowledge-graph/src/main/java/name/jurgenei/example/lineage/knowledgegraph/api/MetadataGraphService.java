package name.jurgenei.example.lineage.knowledgegraph.api;

import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Values;
import org.springframework.stereotype.Service;

@Service
public class MetadataGraphService {

    private static final Pattern NON_ALNUM = Pattern.compile("[^A-Z0-9]+");

    private final Driver driver;

    public MetadataGraphService(Driver driver) {
        this.driver = driver;
    }

    @PostConstruct
    public void ensureSchema() {
        executeWrite("CREATE CONSTRAINT asset_id_unique IF NOT EXISTS FOR (a:Asset) REQUIRE a.assetId IS UNIQUE");
        executeWrite("CREATE INDEX asset_name_idx IF NOT EXISTS FOR (a:Asset) ON (a.name)");
        executeWrite("CREATE INDEX asset_type_idx IF NOT EXISTS FOR (a:Asset) ON (a.assetType)");
        executeWrite("CREATE INDEX asset_source_system_idx IF NOT EXISTS FOR (a:Asset) ON (a.sourceSystem)");
    }

    public AssetResponse createAsset(AssetUpsertRequest request) {
        String assetType = AssetType.parse(request.assetType()).name();
        String assetId = canonicalAssetId(request, assetType);
        if (assetExists(assetId)) {
            throw new ConflictException("Asset already exists: " + assetId);
        }
        String now = Instant.now().toString();
        Map<String, Object> params = new HashMap<>();
        params.put("assetId", assetId);
        params.put("assetType", assetType);
        params.put("name", request.name().trim());
        params.put("sourceSystem", request.sourceSystem().trim());
        params.put("domain", blankToNull(request.domain()));
        params.put("createdAt", now);
        params.put("updatedAt", now);

        Record record = executeReadOne("""
                CREATE (a:Asset {
                  assetId: $assetId,
                  assetType: $assetType,
                  name: $name,
                  sourceSystem: $sourceSystem,
                  domain: $domain,
                  createdAt: $createdAt,
                  updatedAt: $updatedAt
                })
                RETURN a.assetId AS assetId,
                       a.assetType AS assetType,
                       a.name AS name,
                       a.sourceSystem AS sourceSystem,
                       a.domain AS domain,
                       a.createdAt AS createdAt,
                       a.updatedAt AS updatedAt
                """, params);
        return toAsset(record);
    }

    public Optional<AssetResponse> findAssetById(String assetId) {
        if (assetId == null || assetId.isBlank()) {
            return Optional.empty();
        }
        Record record = executeReadMaybe("""
                MATCH (a:Asset {assetId: $assetId})
                RETURN a.assetId AS assetId,
                       a.assetType AS assetType,
                       a.name AS name,
                       a.sourceSystem AS sourceSystem,
                       a.domain AS domain,
                       a.createdAt AS createdAt,
                       a.updatedAt AS updatedAt
                """, Map.of("assetId", assetId.trim()));
        return Optional.ofNullable(record).map(this::toAsset);
    }

    public List<AssetResponse> listAssets() {
        try (Session session = driver.session()) {
            Result result = session.run("""
                    MATCH (a:Asset)
                    RETURN a.assetId AS assetId,
                           a.assetType AS assetType,
                           a.name AS name,
                           a.sourceSystem AS sourceSystem,
                           a.domain AS domain,
                           a.createdAt AS createdAt,
                           a.updatedAt AS updatedAt
                    ORDER BY a.assetId
                    """);
            List<AssetResponse> assets = new ArrayList<>();
            while (result.hasNext()) {
                assets.add(toAsset(result.next()));
            }
            return assets;
        }
    }

    public RelationshipResponse createRelationship(RelationshipCreateRequest request) {
        String relationshipType = RelationshipType.parse(request.relationshipType()).name();
        String sourceAssetId = request.sourceAssetId().trim();
        String targetAssetId = request.targetAssetId().trim();
        if (!assetExists(sourceAssetId)) {
            throw new NotFoundException("Source asset not found: " + sourceAssetId);
        }
        if (!assetExists(targetAssetId)) {
            throw new NotFoundException("Target asset not found: " + targetAssetId);
        }
        String now = Instant.now().toString();
        Record record = executeReadOne("""
                MATCH (s:Asset {assetId: $sourceAssetId})
                MATCH (t:Asset {assetId: $targetAssetId})
                MERGE (s)-[r:RELATED_TO {relationshipType: $relationshipType}]->(t)
                ON CREATE SET r.createdAt = $createdAt, r.updatedAt = $updatedAt
                ON MATCH SET r.updatedAt = $updatedAt
                RETURN s.assetId AS sourceAssetId,
                       t.assetId AS targetAssetId,
                       r.relationshipType AS relationshipType,
                       r.createdAt AS createdAt,
                       r.updatedAt AS updatedAt
                """, Map.of(
                "sourceAssetId", sourceAssetId,
                "targetAssetId", targetAssetId,
                "relationshipType", relationshipType,
                "createdAt", now,
                "updatedAt", now
        ));
        return toRelationship(record);
    }

    public List<RelationshipResponse> listRelationships() {
        try (Session session = driver.session()) {
            Result result = session.run("""
                    MATCH (s:Asset)-[r:RELATED_TO]->(t:Asset)
                    RETURN s.assetId AS sourceAssetId,
                           t.assetId AS targetAssetId,
                           r.relationshipType AS relationshipType,
                           r.createdAt AS createdAt,
                           r.updatedAt AS updatedAt
                    ORDER BY sourceAssetId, targetAssetId, relationshipType
                    """);
            List<RelationshipResponse> relationships = new ArrayList<>();
            while (result.hasNext()) {
                relationships.add(toRelationship(result.next()));
            }
            return relationships;
        }
    }

    private boolean assetExists(String assetId) {
        Record record = executeReadOne(
                "MATCH (a:Asset {assetId: $assetId}) RETURN COUNT(a) AS count",
                Map.of("assetId", assetId)
        );
        return record.get("count").asLong() > 0;
    }

    private void executeWrite(String cypher) {
        try (Session session = driver.session()) {
            session.executeWrite(tx -> {
                tx.run(cypher).consume();
                return null;
            });
        }
    }

    private Record executeReadOne(String cypher, Map<String, Object> params) {
        try (Session session = driver.session()) {
            return session.executeRead(tx -> tx.run(cypher, params).single());
        }
    }

    private Record executeReadMaybe(String cypher, Map<String, Object> params) {
        try (Session session = driver.session()) {
            return session.executeRead(tx -> tx.run(cypher, params).list())
                    .stream()
                    .findFirst()
                    .orElse(null);
        }
    }

    private AssetResponse toAsset(Record record) {
        return new AssetResponse(
                record.get("assetId").asString(),
                record.get("assetType").asString(),
                record.get("name").asString(),
                record.get("sourceSystem").asString(),
                record.get("domain").isNull() ? null : record.get("domain").asString(),
                Instant.parse(record.get("createdAt").asString()),
                Instant.parse(record.get("updatedAt").asString())
        );
    }

    private RelationshipResponse toRelationship(Record record) {
        return new RelationshipResponse(
                record.get("sourceAssetId").asString(),
                record.get("targetAssetId").asString(),
                record.get("relationshipType").asString(),
                Instant.parse(record.get("createdAt").asString()),
                Instant.parse(record.get("updatedAt").asString())
        );
    }

    private String canonicalAssetId(AssetUpsertRequest request, String assetType) {
        if (request.assetId() != null && !request.assetId().isBlank()) {
            return request.assetId().trim();
        }
        String sourceSystem = request.sourceSystem().trim().toUpperCase();
        String normalizedName = NON_ALNUM.matcher(request.name().trim().toUpperCase()).replaceAll("_");
        return sourceSystem + ":" + assetType + ":" + normalizedName;
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
