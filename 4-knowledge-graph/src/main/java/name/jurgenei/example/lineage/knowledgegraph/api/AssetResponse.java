package name.jurgenei.example.lineage.knowledgegraph.api;

import java.time.Instant;

public record AssetResponse(
        String assetId,
        String assetType,
        String name,
        String sourceSystem,
        String domain,
        Instant createdAt,
        Instant updatedAt
) {
}
