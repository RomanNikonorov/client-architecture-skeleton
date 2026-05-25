package me.nikonorov.clients.infrastructure.rest;

import me.nikonorov.http.PooledRestClientRequestFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Инфраструктурная конфигурация для исходящих REST clients.
 *
 * <p>Каждая внешняя REST-система должна иметь именованный bean {@link RestClient}
 * с base URL, timeout и connection pool, настроенными из типизированных
 * свойств. Конкретные clients создаются через auto-configured
 * {@link RestClient.Builder}, который предоставляет Spring Boot.</p>
 */
@Configuration
class RestClientConfiguration {

    private static final Logger EXTERNAL_SYSTEM_C_LOGGER =
            LoggerFactory.getLogger("me.nikonorov.clients.infrastructure.rest.system-c");

    /**
     * Создает {@code RestClient}, используемый адаптером system C.
     *
     * @param restClientBuilder auto-configured builder с настройками Spring Boot
     * @param requestFactory request factory с connection pool для system C
     * @param requestLogger logging interceptor для system C
     * @param properties base URL и timeout-конфигурация для system C
     * @return настроенный REST-клиент для внешней системы C
     */
    @Bean("externalSystemCRestApiClient")
    RestClient externalSystemCRestApiClient(
            RestClient.Builder restClientBuilder,
            @Qualifier("externalSystemCRequestFactory")
            HttpComponentsClientHttpRequestFactory requestFactory,
            @Qualifier("externalSystemCRequestLogger")
            ClientHttpRequestInterceptor requestLogger,
            ExternalRestSystemsProperties properties
    ) {
        return PooledRestClientRequestFactory.restClientBuilder(
                        restClientBuilder,
                        properties.systemC(),
                        requestFactory,
                        requestLogger
                )
                .build();
    }

    /**
     * Создает request factory с отдельным connection pool для system C.
     *
     * @param properties timeout и pool-конфигурация system C
     * @return request factory для {@code externalSystemCRestClient}
     */
    @Bean(destroyMethod = "destroy")
    HttpComponentsClientHttpRequestFactory externalSystemCRequestFactory(
            ExternalRestSystemsProperties properties
    ) {
        return PooledRestClientRequestFactory.requestFactory(properties.systemC());
    }

    /**
     * Создает logging interceptor для исходящих запросов к system C.
     *
     * @return interceptor, который логирует метод, URI, HTTP status и длительность
     */
    @Bean("externalSystemCRequestLogger")
    ClientHttpRequestInterceptor externalSystemCRequestLogger() {
        return (request, body, execution) -> {
            long startedAt = System.nanoTime();
            try {
                ClientHttpResponse response = execution.execute(request, body);
                EXTERNAL_SYSTEM_C_LOGGER.info(
                        "Outbound REST method={} uri={} status={} durationMs={}",
                        request.getMethod(),
                        request.getURI(),
                        response.getStatusCode().value(),
                        elapsedMillis(startedAt)
                );
                return response;
            } catch (IOException | RuntimeException ex) {
                EXTERNAL_SYSTEM_C_LOGGER.warn(
                        "Outbound REST method={} uri={} failed durationMs={}",
                        request.getMethod(),
                        request.getURI(),
                        elapsedMillis(startedAt),
                        ex
                );
                throw ex;
            }
        };
    }

    private static long elapsedMillis(long startedAt) {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt);
    }
}
