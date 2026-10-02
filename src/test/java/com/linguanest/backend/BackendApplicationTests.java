package com.linguanest.backend;

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
// Uses @DynamicPropertySource rather than @ServiceConnection: application.yaml sets an explicit
// spring.datasource.url default, and in CI that combination left Flyway never running at all
// (Hibernate validated against an empty schema - no Flyway log output anywhere). Dynamic
// properties have the highest precedence in Spring's property resolution, unambiguously
// overriding application.yaml's defaults for both the DataSource and Flyway (which falls back
// to spring.datasource.* when spring.flyway.url isn't set) - no auto-wiring to rely on.
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

	@Test
	void contextLoads() {
	}

}
