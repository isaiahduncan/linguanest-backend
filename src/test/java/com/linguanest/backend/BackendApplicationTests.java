package com.linguanest.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

// Was disabled (no Docker/Postgres in the dev sandbox that originally wrote this test) - fixed
// by starting a real Postgres via Testcontainers instead. GitHub Actions runners have Docker,
// so this now gives ci.yaml real coverage of the Flyway migrations and the JPA entity mappings
// against each other (ddl-auto=validate) that the context-load alone previously provided.
@Testcontainers
@SpringBootTest
class BackendApplicationTests {

	@Container
	@ServiceConnection
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

	@Test
	void contextLoads() {
	}

}
