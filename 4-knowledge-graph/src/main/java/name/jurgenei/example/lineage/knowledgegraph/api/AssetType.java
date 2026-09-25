package name.jurgenei.example.lineage.knowledgegraph.api;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public enum AssetType {
    TABLE,
    COLUMN,
    PROCEDURE,
    JOB,
    DOMAIN,
    BUSINESS_TERM,
    COMMUNITY,
    PERSON;

    public static AssetType parse(String rawType) {
        if (rawType == null || rawType.isBlank()) {
            throw new ValidationException("Asset type is required");
        }
        try {
            return AssetType.valueOf(rawType.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Unknown asset type: " + rawType);
        }
    }

    public static Set<String> names() {
        return Arrays.stream(values()).map(Enum::name).collect(Collectors.toSet());
    }
}
