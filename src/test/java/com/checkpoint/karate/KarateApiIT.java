package com.checkpoint.karate;

import com.intuit.karate.junit5.Karate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.profiles.active=dev"
)
class KarateApiIT {

    @LocalServerPort
    int port;

    @Karate.Test
    Karate acceptanceFlows() {
        return Karate.run("classpath:karate/acceptance.feature")
                .systemProperty("karate.baseUrl", "http://localhost:" + port);
    }

    @Karate.Test
    Karate catalogFlows() {
        return Karate.run("classpath:karate/admin/catalog.feature")
                .systemProperty("karate.baseUrl", "http://localhost:" + port);
    }

    @Karate.Test
    Karate authFlows() {
        return Karate.run("classpath:karate/auth/auth.feature")
                .systemProperty("karate.baseUrl", "http://localhost:" + port);
    }

    @Karate.Test
    Karate backlogFlows() {
        return Karate.run("classpath:karate/backlog/backlog.feature")
                .systemProperty("karate.baseUrl", "http://localhost:" + port);
    }

    @Karate.Test
    Karate commonAuthFlows() {
        return Karate.run("classpath:karate/common/auth.feature")
                .systemProperty("karate.baseUrl", "http://localhost:" + port);
    }

    @Karate.Test
    Karate libraryFlows() {
        return Karate.run("classpath:karate/library/library.feature")
                .systemProperty("karate.baseUrl", "http://localhost:" + port);
    }
}
