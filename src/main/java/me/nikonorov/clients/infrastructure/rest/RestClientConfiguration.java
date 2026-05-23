package me.nikonorov.clients.infrastructure.rest;

import me.nikonorov.http.PooledRestClientRequestFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Инфраструктурная конфигурация для исходящих REST clients.
 *
 * <p>Каждая внешняя REST-система должна иметь именованный bean {@link RestClient}
 * с base URL, timeout и connection pool, настроенными из типизированных
 * свойств.</p>
 */
@Configuration
class RestClientConfiguration {

    /**
     * Создает {@code RestClient}, используемый адаптером system C.
     *
     * @param builder builder REST-клиента, предоставленный Spring
     * @param properties base URL и timeout-конфигурация для system C
     * @return настроенный REST-клиент для внешней системы C
     */
    @Bean
    RestClient externalSystemCRestClient(
            RestClient.Builder builder,
            @Qualifier("externalSystemCRequestFactory")
            HttpComponentsClientHttpRequestFactory requestFactory,
            ExternalRestSystemsProperties properties
    ) {
        return builder
                .baseUrl(properties.systemC().baseUrl().toString())
                .requestFactory(requestFactory)
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
        ExternalRestSystemsProperties.SystemConfig systemC = properties.systemC();
        return PooledRestClientRequestFactory.create(
                systemC.connectTimeout(),
                systemC.readTimeout(),
                systemC.poolSize()
        );
    }
}
