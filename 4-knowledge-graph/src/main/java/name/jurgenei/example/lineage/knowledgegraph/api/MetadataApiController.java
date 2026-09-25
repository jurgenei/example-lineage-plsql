package name.jurgenei.example.lineage.knowledgegraph.api;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class MetadataApiController {

    private final MetadataGraphService metadataGraphService;
    private final MetadataValidationService validationService;

    public MetadataApiController(MetadataGraphService metadataGraphService, MetadataValidationService validationService) {
        this.metadataGraphService = metadataGraphService;
        this.validationService = validationService;
    }

    @PostMapping("/assets")
    public ResponseEntity<AssetResponse> createAsset(@RequestBody AssetUpsertRequest request) {
        validationService.validateAsset(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(metadataGraphService.createAsset(request));
    }

    @GetMapping("/assets/{id}")
    public AssetResponse getAsset(@PathVariable("id") String assetId) {
        return metadataGraphService.findAssetById(assetId)
                .orElseThrow(() -> new NotFoundException("Asset not found: " + assetId));
    }

    @GetMapping("/assets")
    public List<AssetResponse> listAssets() {
        return metadataGraphService.listAssets();
    }

    @PostMapping("/relationships")
    public ResponseEntity<RelationshipResponse> createRelationship(@RequestBody RelationshipCreateRequest request) {
        validationService.validateRelationship(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(metadataGraphService.createRelationship(request));
    }

    @GetMapping("/relationships")
    public List<RelationshipResponse> listRelationships() {
        return metadataGraphService.listRelationships();
    }

    @PostMapping("/lineage")
    public ResponseEntity<RelationshipResponse> createLineage(@RequestBody LineageCreateRequest request) {
        validationService.validateLineage(request);
        RelationshipCreateRequest relationshipRequest = new RelationshipCreateRequest(
                request.sourceAssetId(),
                request.targetAssetId(),
                request.relationshipType()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(metadataGraphService.createRelationship(relationshipRequest));
    }

    @PostMapping("/validation")
    public ValidationResult validate(@RequestBody ValidationRequest request) {
        return validationService.validate(request);
    }
}
