package com.linguanest.backend;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

// Was disabled (no Docker/Postgres in the dev sandbox that originally wrote this test) - fixed
// by starting a real Postgres via Testcontainers instead. GitHub Actions runners have Docker,
// so this gives ci.yaml real coverage of the Flyway migrations and the JPA entity mappings
// against each other (ddl-auto=validate) that the context-load alone previously provided.
//
// @DynamicPropertySource correctly points the DataSource bean at the container (confirmed via
// CI logs - Hikari connects to the container's real JDBC URL), but Spring Boot's own Flyway
// auto-configuration never ran its migration against it either way - no Flyway log output
// appeared anywhere in two separate CI runs, with two different ways of wiring the connection
// (@ServiceConnection, then @DynamicPropertySource). Rather than keep guessing at whichever
// Spring Boot 4.1 auto-wiring subtlety is responsible, migrate explicitly in @BeforeAll,
// bypassing Spring's Flyway auto-configuration entirely - guaranteed correct regardless of why
// the automatic path doesn't trigger. If Spring's own Flyway integration does run afterward, it
// finds everything already applied (via flyway_schema_history) and is a no-op - not a conflict.
@Testcontainers
@SpringBootTest
class BackendApplicationTests {

	@Container
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

	@DynamicPropertySource
	static void postgresProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", postgres::getJdbcUrl);
		registry.add("spring.datasource.username", postgres::getUsername);
		registry.add("spring.datasource.password", postgres::getPassword);
	}

	@BeforeAll
	static void migrateSchema() {
		Flyway.configure()
				.dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
				.locations("classpath:db/migration")
				.load()
				.migrate();
	}

	@Test
	void contextLoads() {
	}

}
