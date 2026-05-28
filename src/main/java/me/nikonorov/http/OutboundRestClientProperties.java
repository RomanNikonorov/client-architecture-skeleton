package me.nikonorov.http;

import java.net.URI;
import java.time.Duration;

/**
 * Общая конфигурация исходящего REST-клиента.
 *
 * <p>Record используется как тип вложенного {@code @ConfigurationProperties}
 * блока у конкретного bounded context. Он сохраняет flat YAML-структуру
 * интеграции и централизует дефолты для HTTP transport-параметров.</p>
 *
 * @param baseUrl base URL внешней REST-системы
 * @param connectTimeout timeout на установку HTTP-соединения
 * @param readTimeout timeout на чтение HTTP-ответа
 * @param poolSize максимальный размер connection pool для этого REST-клиента
 * @param maxConnectionsPerRoute максимальный размер connection pool на один route
 * @param idleConnectionEvictionTimeout время простоя соединения перед eviction
 * @param critical нужно ли пробрасывать ошибки адаптера
 */
public record OutboundRestClientProperties(
        URI baseUrl,
        Duration connectTimeout,
        Duration readTimeout,
        int poolSize,
        int maxConnectionsPerRoute,
        Duration idleConnectionEvictionTimeout,
        boolean critical
) {

    /**
     * Дефолтный timeout на установку HTTP-соединения.
     */
    public static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofMillis(300);

    /**
     * Дефолтный timeout на чтение HTTP-ответа.
     */
    public static final Duration DEFAULT_READ_TIMEOUT = Duration.ofMillis(500);

    /**
     * Дефолтный размер connection pool.
     */
    public static final int DEFAULT_POOL_SIZE = 20;

    /**
     * Дефолтное время простоя соединения перед eviction.
     */
    public static final Duration DEFAULT_IDLE_CONNECTION_EVICTION_TIMEOUT = Duration.ofSeconds(30);

    public OutboundRestClientProperties {
        if (baseUrl == null) {
            throw new IllegalArgumentException("baseUrl must be configured for outbound REST client");
        }
        if (connectTimeout == null || connectTimeout.isZero() || connectTimeout.isNegative()) {
            connectTimeout = DEFAULT_CONNECT_TIMEOUT;
        }
        if (readTimeout == null || readTimeout.isZero() || readTimeout.isNegative()) {
            readTimeout = DEFAULT_READ_TIMEOUT;
        }
        if (poolSize < 1) {
            poolSize = DEFAULT_POOL_SIZE;
        }
        if (maxConnectionsPerRoute < 1) {
            maxConnectionsPerRoute = poolSize;
        }
        if (idleConnectionEvictionTimeout == null || idleConnectionEvictionTimeout.isNegative()) {
            idleConnectionEvictionTimeout = DEFAULT_IDLE_CONNECTION_EVICTION_TIMEOUT;
        }
    }

}
