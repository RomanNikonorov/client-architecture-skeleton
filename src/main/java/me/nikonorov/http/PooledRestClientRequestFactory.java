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

    private PooledRestClientRequestFactory() {
    }

    /**
     * Создает request factory с отдельным HTTP connection pool.
     *
     * @param properties нормализованные transport-настройки внешней REST-системы
     * @return request factory для передачи в {@code RestClient.Builder}
     */
    public static HttpComponentsClientHttpRequestFactory requestFactory(
            OutboundRestClientProperties properties
    ) {
        PoolingHttpClientConnectionManager connectionManager = connectionPool(properties);
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(Timeout.of(properties.connectTimeout()))
                .setResponseTimeout(Timeout.of(properties.readTimeout()))
                .build();
        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .evictExpiredConnections()
                .evictIdleConnections(TimeValue.of(properties.idleConnectionEvictionTimeout()))
                .disableAutomaticRetries()
                .build();
        HttpComponentsClientHttpRequestFactory requestFactory =
                new HttpComponentsClientHttpRequestFactory(httpClient);
        requestFactory.setConnectionRequestTimeout(properties.connectTimeout());
        requestFactory.setReadTimeout(properties.readTimeout());
        return requestFactory;
    }

    /**
     * Создает отдельный HTTP connection pool для внешней REST-системы.
     *
     * @param properties нормализованные transport-настройки внешней REST-системы
     * @return connection pool для Apache HttpClient 5
     */
    public static PoolingHttpClientConnectionManager connectionPool(
            OutboundRestClientProperties properties
    ) {
        ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setConnectTimeout(Timeout.of(properties.connectTimeout()))
                .setSocketTimeout(Timeout.of(properties.readTimeout()))
                .build();
        return PoolingHttpClientConnectionManagerBuilder.create()
                .setDefaultConnectionConfig(connectionConfig)
                .setMaxConnTotal(properties.poolSize())
                .setMaxConnPerRoute(properties.maxConnectionsPerRoute())
                .build();
    }
}
