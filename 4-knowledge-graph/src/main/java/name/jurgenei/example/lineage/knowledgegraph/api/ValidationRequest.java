package name.jurgenei.example.lineage.knowledgegraph.api;

public record ValidationRequest(
        AssetUpsertRequest asset,
        RelationshipCreateRequest relationship
) {
}

