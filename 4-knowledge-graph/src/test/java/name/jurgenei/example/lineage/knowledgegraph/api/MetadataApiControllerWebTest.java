package name.jurgenei.example.lineage.knowledgegraph.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = MetadataApiController.class)
@Import({MetadataValidationService.class, MetadataApiExceptionHandler.class})
class MetadataApiControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MetadataGraphService metadataGraphService;

    @Test
    void shouldCreateAsset() throws Exception {
        AssetResponse response = new AssetResponse(
                "ORACLE:TABLE:CUSTOMER",
                "TABLE",
                "CUSTOMER",
                "ORACLE",
                "CUSTOMER_DOMAIN",
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-01T00:00:00Z")
        );
        given(metadataGraphService.createAsset(any())).willReturn(response);

        mockMvc.perform(post("/api/assets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "assetType": "TABLE",
                                  "name": "CUSTOMER",
                                  "sourceSystem": "ORACLE",
                                  "domain": "CUSTOMER_DOMAIN"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.assetId").value("ORACLE:TABLE:CUSTOMER"))
                .andExpect(jsonPath("$.assetType").value("TABLE"));
    }

    @Test
    void shouldReturnValidationErrorForUnknownAssetType() throws Exception {
        mockMvc.perform(post("/api/assets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "assetType": "NOT_SUPPORTED",
                                  "name": "CUSTOMER",
                                  "sourceSystem": "ORACLE",
                                  "domain": "CUSTOMER_DOMAIN"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("VALIDATION_ERROR"));
    }

    @Test
    void shouldReturnAssetById() throws Exception {
        AssetResponse response = new AssetResponse(
                "asset-1",
                "DOMAIN",
                "CUSTOMER_DOMAIN",
                "ORACLE",
                null,
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-01T00:00:00Z")
        );
        given(metadataGraphService.findAssetById("asset-1")).willReturn(Optional.of(response));

        mockMvc.perform(get("/api/assets/asset-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assetType").value("DOMAIN"));
    }

    @Test
    void shouldValidatePayload() throws Exception {
        mockMvc.perform(post("/api/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "asset": {
                                    "assetType": "TABLE",
                                    "name": "",
                                    "sourceSystem": "ORACLE",
                                    "domain": "D"
                                  }
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors[0]").exists());
    }
}
