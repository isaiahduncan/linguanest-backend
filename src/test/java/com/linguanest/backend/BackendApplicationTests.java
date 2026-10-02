package com.linguanest.backend;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("""
		Full context load now requires a real Postgres connection (JPA + Flyway) - there's no \
		Docker/Postgres available in this dev environment or in ci.yaml yet. Re-enable once a \
		Postgres service (e.g. Testcontainers or a CI service container) is wired up.""")
class BackendApplicationTests {

	@Test
	void contextLoads() {
	}

}
