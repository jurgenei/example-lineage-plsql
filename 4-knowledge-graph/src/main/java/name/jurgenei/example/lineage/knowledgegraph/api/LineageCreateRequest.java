package name.jurgenei.example.lineage.knowledgegraph.api;

public record LineageCreateRequest(
        String sourceAssetId,
        String targetAssetId,
        String relationshipType
) {
}
