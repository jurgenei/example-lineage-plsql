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
        String neo4jBuild = Files.readString(root.resolve(Path.of("4-neo4j", "build.gradle")), StandardCharsets.UTF_8);
        assertThat(neo4jBuild).contains("CUSTOMER_REFERENCE->VALID_CUSTOMER");
        assertThat(neo4jBuild).contains("RAW_CUSTOMER->VALID_CUSTOMER");
        assertThat(neo4jBuild).contains("VALID_CUSTOMER->MERGED_CUSTOMER");
        assertThat(neo4jBuild).contains("\"status\": \"pass\"");
    }

    @Test
    void shouldRequirePassStatusInRootGraphVerification() throws Exception {
        Path root = ProjectRootPaths.root();
        String rootBuild = Files.readString(root.resolve("build.gradle"), StandardCharsets.UTF_8);
        assertThat(rootBuild).contains("verificationJson.contains('\"status\": \"pass\"')");
        assertThat(rootBuild).contains("tasks.register('endToEndTest')");
    }
}
