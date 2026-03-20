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
    Karate allFlows() {
        return Karate.run("classpath:karate")
                .systemProperty("karate.baseUrl", "http://localhost:" + port);
    }
}