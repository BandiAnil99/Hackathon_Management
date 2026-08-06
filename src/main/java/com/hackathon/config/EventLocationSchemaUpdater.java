package com.hackathon.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.schema-updater.enabled", havingValue = "true", matchIfMissing = true)
public class EventLocationSchemaUpdater {

    private static final Logger log = LoggerFactory.getLogger(EventLocationSchemaUpdater.class);
    private final JdbcTemplate jdbcTemplate;

    public EventLocationSchemaUpdater(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void updateEventSchema() {
        log.info("SchemaUpdate phase=start table=events columns=location");
        jdbcTemplate.execute("alter table if exists events add column if not exists location varchar(255)");
        log.info("SchemaUpdate phase=done table=events columns=location");
    }
}
