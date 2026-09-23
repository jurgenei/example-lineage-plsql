package name.jurgenei.example.lineage;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class GrowthCrossModuleContractTest {

    @Test
    void shouldDefineArchimateExportContract() throws Exception {
        Path root = ProjectRootPaths.root();
        String architectureBuild = Files.readString(root.resolve(Path.of("1-architecture", "build.gradle")), StandardCharsets.UTF_8);
        assertThat(architectureBuild).contains("assembleArchitectureModel");
        assertThat(architectureBuild).contains("name.jurgenei.gradle.archi");
        assertThat(architectureBuild).contains("name.jurgenei.gradle.xml");
        assertThat(architectureBuild).contains("test-lineage.export.xml");
        assertThat(architectureBuild).contains("test-lineage.sexpr");
        assertThat(architectureBuild).contains("test-lineage.xlsx");
        assertThat(architectureBuild).contains("archi-export/pdf");
    }

    @Test
    void shouldDefineLineageContractAgainstApplicationAssets() throws Exception {
        Path root = ProjectRootPaths.root();
        String applicationBuild = Files.readString(root.resolve(Path.of("2-application", "build.gradle")), StandardCharsets.UTF_8);
        String lineageBuild = Files.readString(root.resolve(Path.of("3-lineage", "build.gradle")), StandardCharsets.UTF_8);
        assertThat(applicationBuild).contains("prepareApplicationAssets");
        assertThat(applicationBuild).contains("application-assets.txt");
        assertThat(lineageBuild).contains("dependsOn(':application:prepareApplicationAssets')");
        assertThat(lineageBuild).contains("asts.xml");
        assertThat(lineageBuild).contains("flowgraphs.json");
        assertThat(lineageBuild).contains("table-lineage.csv");
        assertThat(lineageBuild).contains("lineage.cypher");
    }

    @Test
    void shouldDefineNeo4jContractAgainstArchimateAndLineageOutputs() throws Exception {
        Path root = ProjectRootPaths.root();
        String neo4jBuild = Files.readString(root.resolve(Path.of("4-neo4j", "build.gradle")), StandardCharsets.UTF_8);
        String rootBuild = Files.readString(root.resolve("build.gradle"), StandardCharsets.UTF_8);
        assertThat(neo4jBuild).contains("dependsOn(':architecture:assembleArchitectureModel', ':lineage:generateTableLineage')");
        assertThat(neo4jBuild).contains("verification-report.json");
        assertThat(rootBuild).contains("dependsOn('assembleArchitecture', 'generateLineage', 'startNeo4j')");
    }
}
