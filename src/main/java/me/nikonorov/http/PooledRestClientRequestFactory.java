package me.nikonorov.http;

import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import java.time.Duration;

/**
 * Shared helper для создания production-ready request factory под Spring
 * {@code RestClient}.
 *
 * <p>Helper инкапсулирует Apache HttpClient 5, отдельный connection pool,
 * timeout-настройки и политику очистки idle/expired connections. Bounded
 * contexts передают сюда только уже провалидированные технические параметры
 * своей внешней REST-системы.</p>
 */
public final class PooledRestClientRequestFactory {

    private static final TimeValue IDLE_CONNECTION_EVICTION_TIMEOUT = TimeValue.ofSeconds(30);

    private PooledRestClientRequestFactory() {
    }

    /**
     * Создает request factory с отдельным HTTP connection pool.
     *
     * @param connectTimeout timeout на установку HTTP-соединения
     * @param readTimeout timeout на чтение HTTP-ответа
     * @param poolSize максимальный размер connection pool и per-route limit
     * @return request factory для передачи в {@code RestClient.Builder}
     */
    public static HttpComponentsClientHttpRequestFactory create(
            Duration connectTimeout,
            Duration readTimeout,
            int poolSize
    ) {
        PoolingHttpClientConnectionManager connectionManager = connectionManager(
                connectTimeout,
                readTimeout,
                poolSize
        );
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(Timeout.of(connectTimeout))
                .setResponseTimeout(Timeout.of(readTimeout))
                .build();
        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .evictExpiredConnections()
                .evictIdleConnections(IDLE_CONNECTION_EVICTION_TIMEOUT)
                .disableAutomaticRetries()
                .build();
        HttpComponentsClientHttpRequestFactory requestFactory =
                new HttpComponentsClientHttpRequestFactory(httpClient);
        requestFactory.setConnectionRequestTimeout(connectTimeout);
        requestFactory.setReadTimeout(readTimeout);
        return requestFactory;
    }

    static PoolingHttpClientConnectionManager connectionManager(
            Duration connectTimeout,
            Duration readTimeout,
            int poolSize
    ) {
        ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setConnectTimeout(Timeout.of(connectTimeout))
                .setSocketTimeout(Timeout.of(readTimeout))
                .build();
        return PoolingHttpClientConnectionManagerBuilder.create()
                .setDefaultConnectionConfig(connectionConfig)
                .setMaxConnTotal(poolSize)
                .setMaxConnPerRoute(poolSize)
                .build();
    }
}
