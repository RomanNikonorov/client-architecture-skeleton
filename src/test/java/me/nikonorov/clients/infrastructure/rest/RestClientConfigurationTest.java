package me.nikonorov.clients.infrastructure.rest;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class RestClientConfigurationTest {

    @Test
    void normalizesInvalidPoolSize() {
        ExternalRestSystemsProperties.SystemConfig config = new ExternalRestSystemsProperties.SystemConfig(
                URI.create("http://localhost:9083"),
                Duration.ofMillis(300),
                Duration.ofMillis(500),
                0,
                false
        );

        assertThat(config.poolSize()).isEqualTo(1);
    }
}
