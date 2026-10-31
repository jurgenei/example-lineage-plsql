package org.flowdigger.example.lineage.knowledgegraph.api;

public record RelationshipCreateRequest(
        String sourceAssetId,
        String targetAssetId,
        String relationshipType
) {
}
