package name.jurgenei.example.lineage.knowledgegraph;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

@Service
public class KnowledgeGraphFlowService {

    private static final Set<String> EXPECTED_EDGES = Set.of(
            "CUSTOMER_REFERENCE->VALID_CUSTOMER",
            "RAW_CUSTOMER->VALID_CUSTOMER",
            "VALID_CUSTOMER->MERGED_CUSTOMER"
    );

    private final Driver driver;
    private final ResourceLoader resourceLoader;

    public KnowledgeGraphFlowService(Driver driver, ResourceLoader resourceLoader) {
        this.driver = driver;
        this.resourceLoader = resourceLoader;
    }

    public void loadStubData() {
        runStatement("MATCH (n) DETACH DELETE n");
        loadSeedStatements();
    }

    public Set<String> dumpEdges(Path outputFile) {
        Set<String> edges = queryEdges();
        writeDump(outputFile, edges);
        return edges;
    }

    public void verifyExpectedEdges(Set<String> discoveredEdges) {
        Set<String> missingEdges = EXPECTED_EDGES.stream()
                .filter(edge -> !discoveredEdges.contains(edge))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (!missingEdges.isEmpty()) {
            throw new IllegalStateException("Missing expected knowledge-graph edges: " + missingEdges);
        }
    }

    public void writeLoadReport(Path outputFile) {
        String content = """
                {
                  "status": "loaded",
                  "loadedAt": "%s",
                  "source": "stub/knowledge-graph-seed.cypher"
                }
                """.formatted(Instant.now());
        writeText(outputFile, content);
    }

    public void writeVerificationReport(Path outputFile, Set<String> discoveredEdges) {
        String edgeLines = discoveredEdges.stream()
                .map(edge -> "    \"" + edge + "\"")
                .collect(Collectors.joining(",\n"));
        String content = """
                {
                  "status": "pass",
                  "checks": [
                    "stub-data-load",
                    "expected-knowledge-graph-edges"
                  ],
                  "verifiedEdges": [
                %s
                  ]
                }
                """.formatted(edgeLines);
        writeText(outputFile, content);
    }

    private void loadSeedStatements() {
        Resource resource = resourceLoader.getResource("classpath:stub/knowledge-graph-seed.cypher");
        String cypherText;
        try {
            cypherText = resource.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read stub seed resource", e);
        }
        List<String> statements = List.of(cypherText.split(";")).stream()
                .map(String::trim)
                .filter(statement -> !statement.isEmpty())
                .toList();
        for (String statement : statements) {
            runStatement(statement);
        }
    }

    private Set<String> queryEdges() {
        try (Session session = driver.session()) {
            return session.run("""
                            MATCH (s:DataObject)-[:FLOWS_TO]->(t:DataObject)
                            RETURN s.name AS source, t.name AS target
                            ORDER BY source, target
                            """)
                    .stream()
                    .map(record -> record.get("source").asString() + "->" + record.get("target").asString())
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        }
    }

    private void runStatement(String statement) {
        try (Session session = driver.session()) {
            session.executeWrite(tx -> {
                tx.run(statement).consume();
                return null;
            });
        }
    }

    private void writeDump(Path outputFile, Set<String> edges) {
        String edgeLines = edges.stream()
                .map(edge -> "    \"" + edge + "\"")
                .collect(Collectors.joining(",\n"));
        String content = """
                {
                  "status": "dumped",
                  "edges": [
                %s
                  ]
                }
                """.formatted(edgeLines);
        writeText(outputFile, content);
    }

    private void writeText(Path outputFile, String content) {
        try {
            Files.createDirectories(outputFile.getParent());
            Files.writeString(outputFile, content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to write output file: " + outputFile, e);
        }
    }
}
