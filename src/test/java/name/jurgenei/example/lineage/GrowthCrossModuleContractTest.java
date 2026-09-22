package name.jurgenei.example.lineage;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class GrowthCrossModuleContractTest {

    @Test
    void shouldDefineArchimateExportContract() throws Exception {
        String architectureBuild = Files.readString(Path.of("1-architecture", "build.gradle"), StandardCharsets.UTF_8);
        assertThat(architectureBuild).contains("assembleArchitectureModel");
        assertThat(architectureBuild).contains("application-components.xml");
        assertThat(architectureBuild).contains("stored-procedures.xml");
        assertThat(architectureBuild).contains("relationships.xml");
        assertThat(architectureBuild).contains("archimate-export.json");
    }

    @Test
    void shouldDefineLineageContractAgainstApplicationAssets() throws Exception {
        String applicationBuild = Files.readString(Path.of("2-application", "build.gradle"), StandardCharsets.UTF_8);
        String lineageBuild = Files.readString(Path.of("3-lineage", "build.gradle"), StandardCharsets.UTF_8);
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
        String neo4jBuild = Files.readString(Path.of("4-neo4j", "build.gradle"), StandardCharsets.UTF_8);
        String rootBuild = Files.readString(Path.of("build.gradle"), StandardCharsets.UTF_8);
        assertThat(neo4jBuild).contains("dependsOn(':architecture:assembleArchitectureModel', ':lineage:generateTableLineage')");
        assertThat(neo4jBuild).contains("verification-report.json");
        assertThat(rootBuild).contains("dependsOn('assembleArchitecture', 'generateLineage', 'startNeo4j')");
    }
}
