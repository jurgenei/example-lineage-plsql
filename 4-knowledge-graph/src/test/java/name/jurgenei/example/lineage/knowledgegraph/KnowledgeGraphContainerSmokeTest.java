package name.jurgenei.example.lineage.knowledgegraph;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class KnowledgeGraphContainerSmokeTest {

    @Container
    static final Neo4jContainer<?> NEO4J = new Neo4jContainer<>("neo4j:5.23.0");

    @DynamicPropertySource
    static void configureNeo4j(DynamicPropertyRegistry registry) {
        registry.add("spring.neo4j.uri", NEO4J::getBoltUrl);
        registry.add("spring.neo4j.authentication.username", () -> "neo4j");
        registry.add("spring.neo4j.authentication.password", NEO4J::getAdminPassword);
    }

    @Autowired
    private KnowledgeGraphFlowService flowService;

    @Test
    void shouldLoadDumpAndVerifyStubGraph() throws Exception {
        flowService.loadStubData();
        Path tempFile = Files.createTempFile("knowledge-graph-dump", ".json");
        Set<String> discoveredEdges = flowService.dumpEdges(tempFile);
        flowService.verifyExpectedEdges(discoveredEdges);

        assertThat(discoveredEdges).containsExactlyInAnyOrder(
                "CUSTOMER_REFERENCE->VALID_CUSTOMER",
                "RAW_CUSTOMER->VALID_CUSTOMER",
                "VALID_CUSTOMER->MERGED_CUSTOMER"
        );
        assertThat(Files.exists(tempFile)).isTrue();
    }
}
