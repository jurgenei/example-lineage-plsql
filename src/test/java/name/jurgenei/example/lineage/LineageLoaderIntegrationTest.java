package name.jurgenei.example.lineage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.OracleContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
class LineageLoaderIntegrationTest {

    @Container
    static final OracleContainer ORACLE = new OracleContainer("gvenzl/oracle-xe:21-slim-faststart");

    @DynamicPropertySource
    static void configureDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", ORACLE::getJdbcUrl);
        registry.add("spring.datasource.username", ORACLE::getUsername);
        registry.add("spring.datasource.password", ORACLE::getPassword);
        registry.add("spring.datasource.driver-class-name", ORACLE::getDriverClassName);
    }

    @Autowired
    private LineageLoaderService lineageLoaderService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldLoadCustomerLineageWithExpectedMappings() {
        lineageLoaderService.runLoad();

        List<Map<String, Object>> validRows = jdbcTemplate.queryForList("""
                select customer_id, credit_rating, risk_segment, customer_status, load_ts, valid_customer_sk
                from valid_customer
                order by customer_id
                """);

        assertThat(validRows).hasSize(3);
        assertThat(validRows).extracting(
                row -> row.get("CUSTOMER_ID"),
                row -> row.get("CREDIT_RATING"),
                row -> row.get("RISK_SEGMENT"),
                row -> row.get("CUSTOMER_STATUS")
        ).containsExactly(
                tuple("C001", "AAA", "LOW", "ACTIVE"),
                tuple("C002", "BBB", "MEDIUM", "ACTIVE"),
                tuple("C999", null, null, "UNMATCHED")
        );
        assertThat(validRows).allMatch(row -> row.get("LOAD_TS") != null);
        assertThat(validRows).allMatch(row -> row.get("VALID_CUSTOMER_SK") != null);

        List<Map<String, Object>> mergedRows = jdbcTemplate.queryForList("""
                select valid_customer_sk, customer_id, credit_rating, risk_segment, load_ts
                from merged_customer
                order by customer_id
                """);

        assertThat(mergedRows).hasSize(3);
        assertThat(mergedRows).extracting(
                row -> row.get("CUSTOMER_ID"),
                row -> row.get("CREDIT_RATING"),
                row -> row.get("RISK_SEGMENT")
        ).containsExactly(
                tuple("C001", "AAA", "LOW"),
                tuple("C002", "BBB", "MEDIUM"),
                tuple("C999", null, null)
        );
        assertThat(mergedRows).allMatch(row -> row.get("LOAD_TS") != null);
        assertThat(mergedRows).allMatch(row -> row.get("VALID_CUSTOMER_SK") != null);
    }
}
