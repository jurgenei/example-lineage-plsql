package name.jurgenei.example.lineage.knowledgegraph.api;

import java.time.Instant;

public record RelationshipResponse(
        String sourceAssetId,
        String targetAssetId,
        String relationshipType,
        Instant createdAt,
        Instant updatedAt
) {
}
