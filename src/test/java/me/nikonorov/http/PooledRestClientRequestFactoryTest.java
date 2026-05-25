package me.nikonorov.http;

import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class PooledRestClientRequestFactoryTest {

    @Test
    void createsRequestFactoryWithHttpClient() {
        HttpComponentsClientHttpRequestFactory requestFactory = PooledRestClientRequestFactory.create(
                Duration.ofMillis(300),
                Duration.ofMillis(500),
                7,
                3,
                Duration.ofSeconds(30)
        );

        assertThat(requestFactory.getHttpClient()).isNotNull();
    }

    @Test
    void configuresConnectionPoolLimits() {
        PoolingHttpClientConnectionManager connectionManager =
                PooledRestClientRequestFactory.connectionManager(
                        Duration.ofMillis(300),
                        Duration.ofMillis(500),
                        11,
                        5
                );

        assertThat(connectionManager.getMaxTotal()).isEqualTo(11);
        assertThat(connectionManager.getDefaultMaxPerRoute()).isEqualTo(5);
    }
}
