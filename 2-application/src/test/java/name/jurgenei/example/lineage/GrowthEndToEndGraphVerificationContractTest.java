package name.jurgenei.example.lineage;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class GrowthEndToEndGraphVerificationContractTest {

    @Test
    void shouldDeclareExpectedLineageEdgesForGraphVerification() throws Exception {
        Path root = ProjectRootPaths.root();
        String knowledgeGraphBuild = Files.readString(root.resolve(Path.of("4-knowledge-graph", "build.gradle")), StandardCharsets.UTF_8);
        assertThat(knowledgeGraphBuild).contains("CUSTOMER_REFERENCE->VALID_CUSTOMER");
        assertThat(knowledgeGraphBuild).contains("RAW_CUSTOMER->VALID_CUSTOMER");
        assertThat(knowledgeGraphBuild).contains("VALID_CUSTOMER->MERGED_CUSTOMER");
        assertThat(knowledgeGraphBuild).contains("\"status\": \"pass\"");
    }

    @Test
    void shouldRequirePassStatusInRootGraphVerification() throws Exception {
        Path root = ProjectRootPaths.root();
        String rootBuild = Files.readString(root.resolve("build.gradle"), StandardCharsets.UTF_8);
        assertThat(rootBuild).contains("verificationJson.contains('\"status\": \"pass\"')");
        assertThat(rootBuild).contains("tasks.register('endToEndTest')");
    }
}
