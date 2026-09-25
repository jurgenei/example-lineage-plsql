package name.jurgenei.example.lineage.knowledgegraph.api;

public record RelationshipCreateRequest(
        String sourceAssetId,
        String targetAssetId,
        String relationshipType
) {
}
