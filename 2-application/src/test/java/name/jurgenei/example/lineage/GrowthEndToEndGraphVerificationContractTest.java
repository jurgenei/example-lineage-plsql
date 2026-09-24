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
        String seedCypher = Files.readString(root.resolve(Path.of("4-knowledge-graph", "src", "main", "resources", "stub", "knowledge-graph-seed.cypher")), StandardCharsets.UTF_8);
        String knowledgeGraphFlow = Files.readString(root.resolve(Path.of("4-knowledge-graph", "src", "main", "java", "name", "jurgenei", "example", "lineage", "knowledgegraph", "KnowledgeGraphFlowService.java")), StandardCharsets.UTF_8);
        assertThat(seedCypher).contains("CUSTOMER_REFERENCE");
        assertThat(seedCypher).contains("RAW_CUSTOMER");
        assertThat(seedCypher).contains("VALID_CUSTOMER");
        assertThat(seedCypher).contains("MERGED_CUSTOMER");
        assertThat(knowledgeGraphFlow).contains("\"status\": \"pass\"");
    }

    @Test
    void shouldRequirePassStatusInRootGraphVerification() throws Exception {
        Path root = ProjectRootPaths.root();
        String rootBuild = Files.readString(root.resolve("build.gradle"), StandardCharsets.UTF_8);
        assertThat(rootBuild).contains("verificationJson.contains('\"status\": \"pass\"')");
        assertThat(rootBuild).contains("tasks.register('endToEndTest')");
    }
}
