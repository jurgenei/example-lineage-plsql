package name.jurgenei.example.lineage;

import java.nio.file.Files;
import java.nio.file.Path;

final class ProjectRootPaths {

    private ProjectRootPaths() {
    }

    static Path root() {
        Path current = Path.of("").toAbsolutePath().normalize();
        while (current != null && !Files.exists(current.resolve("settings.gradle"))) {
            current = current.getParent();
        }
        if (current == null) {
            throw new IllegalStateException("Unable to resolve repository root from current working directory.");
        }
        return current;
    }
}
