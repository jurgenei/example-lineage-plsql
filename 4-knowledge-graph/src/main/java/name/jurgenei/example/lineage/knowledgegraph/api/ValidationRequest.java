package org.flowdigger.example.lineage.knowledgegraph.api;

public record ValidationRequest(
        AssetUpsertRequest asset,
        RelationshipCreateRequest relationship
) {
}

