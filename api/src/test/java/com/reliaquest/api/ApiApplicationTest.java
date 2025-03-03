package com.reliaquest.api;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApiApplicationTest {

    @LocalServerPort
    private int port;

    @Test
    void applicationStartsSuccessfully() {
        assertThat(port).isGreaterThan(0);
    }
}
