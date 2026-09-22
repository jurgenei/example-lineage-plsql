package name.jurgenei.example.lineage;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class GrowthStructureBootstrapTest {

    @Test
    void shouldCreateExpectedGrowthSubprojectLayout() {
        Path root = ProjectRootPaths.root();
        assertThat(Files.isDirectory(root.resolve("1-architecture"))).isTrue();
        assertThat(Files.isDirectory(root.resolve("2-application"))).isTrue();
        assertThat(Files.isDirectory(root.resolve("3-lineage"))).isTrue();
        assertThat(Files.isDirectory(root.resolve("4-neo4j"))).isTrue();

        assertThat(Files.exists(root.resolve(Path.of("1-architecture", "build.gradle")))).isTrue();
        assertThat(Files.exists(root.resolve(Path.of("2-application", "build.gradle")))).isTrue();
        assertThat(Files.exists(root.resolve(Path.of("3-lineage", "build.gradle")))).isTrue();
        assertThat(Files.exists(root.resolve(Path.of("4-neo4j", "build.gradle")))).isTrue();
    }
}
