package name.jurgenei.example.lineage.knowledgegraph.api;

import java.util.Set;

public enum RelationshipType {
    READS,
    WRITES,
    CALLS,
    CONTAINS,
    BELONGS_TO,
    OWNED_BY,
    DESCRIBES,
    PART_OF,
    SAME_AS;

    private static final Set<RelationshipType> LINEAGE_TYPES = Set.of(READS, WRITES, CALLS, CONTAINS);

    public static RelationshipType parse(String rawType) {
        if (rawType == null || rawType.isBlank()) {
            throw new ValidationException("Relationship type is required");
        }
        try {
            return RelationshipType.valueOf(rawType.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid relationship type: " + rawType);
        }
    }

    public static RelationshipType parseLineage(String rawType) {
        RelationshipType parsed = parse(rawType);
        if (!LINEAGE_TYPES.contains(parsed)) {
            throw new ValidationException("Lineage relationship type must be one of " + LINEAGE_TYPES);
        }
        return parsed;
    }
}
