package com.example.lineage;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class GrowthStructureBootstrapTest {

    @Test
    void shouldCreateExpectedGrowthSubprojectLayout() {
        assertThat(Files.isDirectory(Path.of("application"))).isTrue();
        assertThat(Files.isDirectory(Path.of("archimate"))).isTrue();
        assertThat(Files.isDirectory(Path.of("lineage"))).isTrue();
        assertThat(Files.isDirectory(Path.of("neo4j"))).isTrue();

        assertThat(Files.exists(Path.of("application", "build.gradle"))).isTrue();
        assertThat(Files.exists(Path.of("archimate", "build.gradle"))).isTrue();
        assertThat(Files.exists(Path.of("lineage", "build.gradle"))).isTrue();
        assertThat(Files.exists(Path.of("neo4j", "build.gradle"))).isTrue();
    }
}
