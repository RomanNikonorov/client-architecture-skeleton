package me.nikonorov.http;

import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import java.net.URI;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class PooledRestClientRequestFactoryTest {

    @Test
    void createsRequestFactoryFromProperties() {
        OutboundRestClientProperties properties = new OutboundRestClientProperties(
                URI.create("http://localhost:9083"),
                Duration.ofMillis(300),
                Duration.ofMillis(500),
                7,
                3,
                Duration.ofSeconds(30),
                false
        );

        HttpComponentsClientHttpRequestFactory requestFactory =
                PooledRestClientRequestFactory.requestFactory(properties);

        assertThat(requestFactory.getHttpClient()).isNotNull();
    }

    @Test
    void createsConnectionPoolFromProperties() {
        OutboundRestClientProperties properties = new OutboundRestClientProperties(
                URI.create("http://localhost:9083"),
                Duration.ofMillis(300),
                Duration.ofMillis(500),
                11,
                5,
                Duration.ofSeconds(30),
                false
        );

        PoolingHttpClientConnectionManager connectionManager =
                PooledRestClientRequestFactory.connectionPool(properties);

        assertThat(connectionManager.getMaxTotal()).isEqualTo(11);
        assertThat(connectionManager.getDefaultMaxPerRoute()).isEqualTo(5);
    }
}
