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
 * @param idleConnectionEvictionTimeout время простоя соединения перед eviction
 * @param critical нужно ли пробрасывать ошибки адаптера
 */
public record OutboundRestClientProperties(
        URI baseUrl,
        Duration connectTimeout,
        Duration readTimeout,
        int poolSize,
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
        if (connectTimeout == null || connectTimeout.isZero() || connectTimeout.isNegative()) {
            connectTimeout = DEFAULT_CONNECT_TIMEOUT;
        }
        if (readTimeout == null || readTimeout.isZero() || readTimeout.isNegative()) {
            readTimeout = DEFAULT_READ_TIMEOUT;
        }
        if (poolSize < 1) {
            poolSize = DEFAULT_POOL_SIZE;
        }
        if (idleConnectionEvictionTimeout == null || idleConnectionEvictionTimeout.isNegative()) {
            idleConnectionEvictionTimeout = DEFAULT_IDLE_CONNECTION_EVICTION_TIMEOUT;
        }
    }

    /**
     * Создает конфигурацию с дефолтными HTTP transport-параметрами.
     *
     * @param baseUrl default base URL конкретной внешней системы
     * @return нормализованная конфигурация исходящего REST-клиента
     */
    public static OutboundRestClientProperties withDefaultHttpParameters(URI baseUrl) {
        return new OutboundRestClientProperties(
                baseUrl,
                DEFAULT_CONNECT_TIMEOUT,
                DEFAULT_READ_TIMEOUT,
                DEFAULT_POOL_SIZE,
                DEFAULT_IDLE_CONNECTION_EVICTION_TIMEOUT,
                false
        );
    }

    /**
     * Подставляет default base URL конкретной интеграции, если он не задан.
     *
     * @param defaultBaseUrl default base URL внешней REST-системы
     * @return текущая конфигурация или копия с подставленным {@code baseUrl}
     */
    public OutboundRestClientProperties withDefaultBaseUrl(URI defaultBaseUrl) {
        if (baseUrl != null) {
            return this;
        }
        return new OutboundRestClientProperties(
                defaultBaseUrl,
                connectTimeout,
                readTimeout,
                poolSize,
                idleConnectionEvictionTimeout,
                critical
        );
    }
}
