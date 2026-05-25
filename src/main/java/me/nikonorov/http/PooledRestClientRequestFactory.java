package me.nikonorov.http;

import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Shared helper для настройки production-ready {@code RestClient}.
 *
 * <p>Helper инкапсулирует повторяемую настройку {@link RestClient.Builder},
 * Apache HttpClient 5, отдельный connection pool, timeout-настройки и
 * политику очистки idle/expired connections. Bounded contexts передают сюда
 * только уже провалидированные технические параметры и interceptors своей
 * внешней REST-системы.</p>
 */
public final class PooledRestClientRequestFactory {

    private PooledRestClientRequestFactory() {
    }

    /**
     * Настраивает {@link RestClient.Builder} для исходящей REST-интеграции.
     *
     * <p>Метод сохраняет Boot customizers, которые уже применены к переданному
     * builder, и добавляет integration-specific base URL, request factory и
     * interceptors, созданные приложением для конкретной интеграции.</p>
     *
     * @param restClientBuilder auto-configured builder от Spring Boot
     * @param properties нормализованные transport-настройки внешней REST-системы
     * @param requestFactory request factory с connection pool и timeout
     * @param interceptors interceptors конкретной интеграции
     * @return настроенный builder
     */
    public static RestClient.Builder restClientBuilder(
            RestClient.Builder restClientBuilder,
            OutboundRestClientProperties properties,
            ClientHttpRequestFactory requestFactory,
            ClientHttpRequestInterceptor... interceptors
    ) {
        Objects.requireNonNull(restClientBuilder, "restClientBuilder must not be null");
        Objects.requireNonNull(properties, "properties must not be null");
        Objects.requireNonNull(properties.baseUrl(), "properties.baseUrl must not be null");
        Objects.requireNonNull(requestFactory, "requestFactory must not be null");

        List<ClientHttpRequestInterceptor> configuredInterceptors = interceptors == null
                ? List.of()
                : Arrays.stream(interceptors)
                        .filter(Objects::nonNull)
                        .toList();

        return restClientBuilder
                .baseUrl(properties.baseUrl())
                .requestFactory(requestFactory)
                .requestInterceptors(existingInterceptors ->
                        existingInterceptors.addAll(configuredInterceptors));
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

    private static PoolingHttpClientConnectionManager connectionPool(
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
