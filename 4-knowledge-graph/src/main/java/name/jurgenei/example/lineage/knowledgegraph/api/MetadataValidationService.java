package name.jurgenei.example.lineage.knowledgegraph.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class MetadataValidationService {

    private static final Set<AssetType> DOMAIN_REQUIRED_TYPES = Set.of(
            AssetType.TABLE,
            AssetType.COLUMN,
            AssetType.PROCEDURE,
            AssetType.JOB,
            AssetType.BUSINESS_TERM
    );

    public void validateAsset(AssetUpsertRequest request) {
        if (request == null) {
            throw new ValidationException("Asset payload is required");
        }
        AssetType type = AssetType.parse(request.assetType());
        if (isBlank(request.name())) {
            throw new ValidationException("Asset name must not be empty");
        }
        if (isBlank(request.sourceSystem())) {
            throw new ValidationException("Source system must not be empty");
        }
        if (DOMAIN_REQUIRED_TYPES.contains(type) && isBlank(request.domain())) {
            throw new ValidationException("Missing domain for asset type " + type);
        }
    }

    public void validateRelationship(RelationshipCreateRequest request) {
        if (request == null) {
            throw new ValidationException("Relationship payload is required");
        }
        if (isBlank(request.sourceAssetId())) {
            throw new ValidationException("Source asset ID is required");
        }
        if (isBlank(request.targetAssetId())) {
            throw new ValidationException("Target asset ID is required");
        }
        if (request.sourceAssetId().trim().equals(request.targetAssetId().trim())) {
            throw new ValidationException("Relationship must target a different asset");
        }
        RelationshipType.parse(request.relationshipType());
    }

    public void validateLineage(LineageCreateRequest request) {
        if (request == null) {
            throw new ValidationException("Lineage payload is required");
        }
        validateRelationship(new RelationshipCreateRequest(
                request.sourceAssetId(),
                request.targetAssetId(),
                request.relationshipType()
        ));
        RelationshipType.parseLineage(request.relationshipType());
    }

    public ValidationResult validate(ValidationRequest request) {
        if (request == null) {
            return ValidationResult.invalid(List.of("Validation payload is required"));
        }
        List<String> errors = new ArrayList<>();
        if (request.asset() != null) {
            collectValidationError(errors, () -> validateAsset(request.asset()));
        }
        if (request.relationship() != null) {
            collectValidationError(errors, () -> validateRelationship(request.relationship()));
        }
        if (request.asset() == null && request.relationship() == null) {
            errors.add("Provide asset and/or relationship payload for validation");
        }
        return errors.isEmpty() ? ValidationResult.valid() : ValidationResult.invalid(errors);
    }

    private void collectValidationError(List<String> errors, Runnable validator) {
        try {
            validator.run();
        } catch (ValidationException e) {
            errors.add(e.getMessage());
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
