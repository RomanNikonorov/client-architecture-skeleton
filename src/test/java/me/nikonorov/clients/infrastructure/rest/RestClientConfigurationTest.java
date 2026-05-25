package me.nikonorov.clients.infrastructure.rest;

import me.nikonorov.http.OutboundRestClientProperties;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class RestClientConfigurationTest {

    @Test
    void usesDefaultsWhenSystemConfigIsMissing() {
        ExternalRestSystemsProperties properties = new ExternalRestSystemsProperties(null);

        assertThat(properties.systemC().baseUrl()).isEqualTo(URI.create("http://localhost:9083"));
        assertThat(properties.systemC().connectTimeout()).isEqualTo(Duration.ofMillis(300));
        assertThat(properties.systemC().readTimeout()).isEqualTo(Duration.ofMillis(500));
        assertThat(properties.systemC().poolSize()).isEqualTo(20);
        assertThat(properties.systemC().maxConnectionsPerRoute()).isEqualTo(20);
        assertThat(properties.systemC().idleConnectionEvictionTimeout()).isEqualTo(Duration.ofSeconds(30));
    }

    @Test
    void normalizesInvalidHttpParametersToDefaults() {
        OutboundRestClientProperties config = new ExternalRestSystemsProperties(
                new OutboundRestClientProperties(
                        null,
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
