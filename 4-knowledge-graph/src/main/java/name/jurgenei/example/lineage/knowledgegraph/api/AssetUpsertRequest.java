package org.flowdigger.example.lineage.knowledgegraph.api;

public record AssetUpsertRequest(
        String assetId,
        String assetType,
        String name,
        String sourceSystem,
        String domain
) {
}
