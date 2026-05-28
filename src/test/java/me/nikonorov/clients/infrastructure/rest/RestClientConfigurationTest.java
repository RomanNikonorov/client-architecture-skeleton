package me.nikonorov.clients.infrastructure.rest;

import me.nikonorov.http.OutboundRestClientProperties;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RestClientConfigurationTest {

    @Test
    void failsWhenBaseUrlIsMissing() {
        assertThatThrownBy(() -> new OutboundRestClientProperties(
                null,
                Duration.ofMillis(300),
                Duration.ofMillis(500),
                20,
                20,
                Duration.ofSeconds(30),
                false
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("baseUrl must be configured for outbound REST client");
    }

    @Test
    void normalizesInvalidHttpParametersToDefaults() {
        OutboundRestClientProperties config = new ExternalRestSystemsProperties(
                new OutboundRestClientProperties(
                        URI.create("http://localhost:9083"),
                        Duration.ZERO,
                        Duration.ofMillis(-1),
                        0,
                        0,
                        Duration.ofMillis(-1),
                        false
                )
        ).systemC();

        assertThat(config.baseUrl()).isEqualTo(URI.create("http://localhost:9083"));
        assertThat(config.connectTimeout()).isEqualTo(Duration.ofMillis(300));
        assertThat(config.readTimeout()).isEqualTo(Duration.ofMillis(500));
        assertThat(config.poolSize()).isEqualTo(20);
        assertThat(config.maxConnectionsPerRoute()).isEqualTo(20);
        assertThat(config.idleConnectionEvictionTimeout()).isEqualTo(Duration.ofSeconds(30));
    }

    @Test
    void keepsExplicitHttpParameters() {
        OutboundRestClientProperties config = new OutboundRestClientProperties(
                URI.create("http://localhost:9183"),
                Duration.ofMillis(100),
                Duration.ofMillis(200),
                3,
                2,
                Duration.ZERO,
                false
        );

        assertThat(config.baseUrl()).isEqualTo(URI.create("http://localhost:9183"));
        assertThat(config.connectTimeout()).isEqualTo(Duration.ofMillis(100));
        assertThat(config.readTimeout()).isEqualTo(Duration.ofMillis(200));
        assertThat(config.poolSize()).isEqualTo(3);
        assertThat(config.maxConnectionsPerRoute()).isEqualTo(2);
        assertThat(config.idleConnectionEvictionTimeout()).isEqualTo(Duration.ZERO);
    }
}
