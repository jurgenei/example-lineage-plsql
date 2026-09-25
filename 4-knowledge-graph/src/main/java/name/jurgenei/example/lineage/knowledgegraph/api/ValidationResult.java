package name.jurgenei.example.lineage.knowledgegraph.api;

import java.util.List;

public record ValidationResult(
        String status,
        List<String> errors
) {
    public static ValidationResult valid() {
        return new ValidationResult("VALID", List.of());
    }

    public static ValidationResult invalid(List<String> errors) {
        return new ValidationResult("VALIDATION_ERROR", errors);
    }
}
