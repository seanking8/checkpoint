package com.checkpoint.karate;

import com.intuit.karate.junit5.Karate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class KarateApiTest {

    @LocalServerPort
    int port;

    @Karate.Test
    Karate authFlow() {
        return Karate.run("classpath:karate/auth/auth.feature")
                .systemProperty("karate.baseUrl", "http://localhost:" + port);
    }
}

