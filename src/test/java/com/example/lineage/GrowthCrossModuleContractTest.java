package com.example.lineage;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class GrowthCrossModuleContractTest {

    @Test
    void shouldDefineArchimateExportContract() throws Exception {
        String archimateBuild = Files.readString(Path.of("archimate", "build.gradle"), StandardCharsets.UTF_8);
        assertThat(archimateBuild).contains("assembleArchitectureModel");
        assertThat(archimateBuild).contains("application-components.xml");
        assertThat(archimateBuild).contains("stored-procedures.xml");
        assertThat(archimateBuild).contains("relationships.xml");
        assertThat(archimateBuild).contains("archimate-export.json");
    }

    @Test
    void shouldDefineLineageContractAgainstApplicationAssets() throws Exception {
        String applicationBuild = Files.readString(Path.of("application", "build.gradle"), StandardCharsets.UTF_8);
        String lineageBuild = Files.readString(Path.of("lineage", "build.gradle"), StandardCharsets.UTF_8);
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
        String neo4jBuild = Files.readString(Path.of("neo4j", "build.gradle"), StandardCharsets.UTF_8);
        String rootBuild = Files.readString(Path.of("build.gradle"), StandardCharsets.UTF_8);
        assertThat(neo4jBuild).contains("dependsOn(':archimate:assembleArchitectureModel', ':lineage:generateTableLineage')");
        assertThat(neo4jBuild).contains("verification-report.json");
        assertThat(rootBuild).contains("dependsOn('assembleArchitecture', 'generateLineage', 'startNeo4j')");
    }
}
