package com.example.lineage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class LineageLoaderRepository {

    private final JdbcTemplate jdbcTemplate;

    public LineageLoaderRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void loadCustomerLineage() {
        jdbcTemplate.execute("begin load_customer_lineage; end;");
    }
}
