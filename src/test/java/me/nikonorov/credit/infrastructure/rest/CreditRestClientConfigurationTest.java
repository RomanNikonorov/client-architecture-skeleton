package me.nikonorov.credit.infrastructure.rest;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class CreditRestClientConfigurationTest {

    @Test
    void normalizesInvalidPoolSize() {
        CreditRestSystemsProperties.SystemConfig config = new CreditRestSystemsProperties.SystemConfig(
                URI.create("http://localhost:9084"),
                Duration.ofMillis(300),
                Duration.ofMillis(500),
                -1,
                false
        );

        assertThat(config.poolSize()).isEqualTo(1);
    }
}
