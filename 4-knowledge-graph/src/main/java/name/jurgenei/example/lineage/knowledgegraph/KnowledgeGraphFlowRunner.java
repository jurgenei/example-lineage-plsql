package name.jurgenei.example.lineage.knowledgegraph;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Set;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.Neo4jContainer;

public final class KnowledgeGraphFlowRunner {

    private KnowledgeGraphFlowRunner() {
    }

    public static void main(String[] args) {
        String action = args.length > 0 ? args[0].trim().toLowerCase() : "verify";
        Path outputDir = args.length > 1 ? Path.of(args[1]) : Path.of("build/graph-report");

        try (Neo4jContainer<?> neo4j = new Neo4jContainer<>("neo4j:5.23.0")) {
            neo4j.start();
            writeContainerStartReport(outputDir.resolve("container-start-report.json"), neo4j);

            if ("start".equals(action)) {
                return;
            }

            try (ConfigurableApplicationContext context = new SpringApplicationBuilder(KnowledgeGraphApplication.class)
                    .web(WebApplicationType.NONE)
                    .properties(
                            "spring.neo4j.uri=" + neo4j.getBoltUrl(),
                            "spring.neo4j.authentication.username=neo4j",
                            "spring.neo4j.authentication.password=" + neo4j.getAdminPassword()
                    )
                    .run()) {
                KnowledgeGraphFlowService flowService = context.getBean(KnowledgeGraphFlowService.class);
                flowService.loadStubData();
                flowService.writeLoadReport(outputDir.resolve("load-report.json"));

                if ("load".equals(action)) {
                    return;
                }

                Set<String> discoveredEdges = flowService.dumpEdges(outputDir.resolve("graph-dump.json"));
                if ("dump".equals(action)) {
                    return;
                }

                if ("verify".equals(action) || "all".equals(action)) {
                    flowService.verifyExpectedEdges(discoveredEdges);
                    flowService.writeVerificationReport(outputDir.resolve("verification-report.json"), discoveredEdges);
                    return;
                }

                throw new IllegalArgumentException("Unsupported action: " + action);
            }
        }
    }

    private static void writeContainerStartReport(Path outputFile, Neo4jContainer<?> neo4j) {
        String content = """
                {
                  "status": "started",
                  "startedAt": "%s",
                  "image": "%s",
                  "boltUrl": "%s"
                }
                """.formatted(Instant.now(), neo4j.getDockerImageName(), neo4j.getBoltUrl());
        try {
            Path parent = outputFile.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(outputFile, content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to write container start report", e);
        }
    }
}
