package name.jurgenei.example.lineage.knowledgegraph.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class MetadataValidationServiceTest {

    private final MetadataValidationService validationService = new MetadataValidationService();

    @Test
    void shouldRejectUnknownAssetType() {
        AssetUpsertRequest request = new AssetUpsertRequest(null, "UNKNOWN", "CUSTOMER", "ORACLE", "CUSTOMER_DOMAIN");

        assertThatThrownBy(() -> validationService.validateAsset(request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Unknown asset type");
    }

    @Test
    void shouldRejectMissingDomainForTable() {
        AssetUpsertRequest request = new AssetUpsertRequest(null, "TABLE", "CUSTOMER", "ORACLE", null);

        assertThatThrownBy(() -> validationService.validateAsset(request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Missing domain");
    }

    @Test
    void shouldAcceptDomainAssetWithoutDomainField() {
        AssetUpsertRequest request = new AssetUpsertRequest("dom-1", "DOMAIN", "CUSTOMER_DOMAIN", "ORACLE", null);

        validationService.validateAsset(request);
    }

    @Test
    void shouldRejectInvalidRelationship() {
        RelationshipCreateRequest request = new RelationshipCreateRequest("A", "A", "READS");

        assertThatThrownBy(() -> validationService.validateRelationship(request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("different asset");
    }

    @Test
    void shouldReturnStructuredValidationErrors() {
        ValidationRequest request = new ValidationRequest(
                new AssetUpsertRequest(null, "X", "N", "ORACLE", "D"),
                new RelationshipCreateRequest("A", "A", "READS")
        );

        ValidationResult result = validationService.validate(request);

        assertThat(result.status()).isEqualTo("VALIDATION_ERROR");
        assertThat(result.errors()).hasSize(2);
    }
}
