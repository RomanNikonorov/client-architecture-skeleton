package me.nikonorov.credit.infrastructure.rest;

import me.nikonorov.http.OutboundRestClientProperties;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class CreditRestClientConfigurationTest {

    @Test
    void usesDefaultsWhenSystemConfigIsMissing() {
        CreditRestSystemsProperties properties = new CreditRestSystemsProperties(null);

        assertThat(properties.pricing().baseUrl()).isEqualTo(URI.create("http://localhost:9084"));
        assertThat(properties.pricing().connectTimeout()).isEqualTo(Duration.ofMillis(300));
        assertThat(properties.pricing().readTimeout()).isEqualTo(Duration.ofMillis(500));
        assertThat(properties.pricing().poolSize()).isEqualTo(20);
        assertThat(properties.pricing().idleConnectionEvictionTimeout()).isEqualTo(Duration.ofSeconds(30));
    }

    @Test
    void normalizesInvalidHttpParametersToDefaults() {
        OutboundRestClientProperties config = new CreditRestSystemsProperties(
                new OutboundRestClientProperties(
                        null,
                        Duration.ZERO,
                        Duration.ofMillis(-1),
                        -1,
                        Duration.ofMillis(-1),
                        false
                )
        ).pricing();

        assertThat(config.baseUrl()).isEqualTo(URI.create("http://localhost:9084"));
        assertThat(config.connectTimeout()).isEqualTo(Duration.ofMillis(300));
        assertThat(config.readTimeout()).isEqualTo(Duration.ofMillis(500));
        assertThat(config.poolSize()).isEqualTo(20);
        assertThat(config.idleConnectionEvictionTimeout()).isEqualTo(Duration.ofSeconds(30));
    }

    @Test
    void keepsExplicitHttpParameters() {
        OutboundRestClientProperties config = new OutboundRestClientProperties(
                URI.create("http://localhost:9184"),
                Duration.ofMillis(100),
                Duration.ofMillis(200),
                3,
                Duration.ZERO,
                false
        );

        assertThat(config.baseUrl()).isEqualTo(URI.create("http://localhost:9184"));
        assertThat(config.connectTimeout()).isEqualTo(Duration.ofMillis(100));
        assertThat(config.readTimeout()).isEqualTo(Duration.ofMillis(200));
        assertThat(config.poolSize()).isEqualTo(3);
        assertThat(config.idleConnectionEvictionTimeout()).isEqualTo(Duration.ZERO);
    }
}
