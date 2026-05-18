package me.nikonorov.clients.client.infrastructure.rest;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Инфраструктурная конфигурация для исходящих REST clients.
 *
 * <p>Каждая внешняя REST-система должна иметь именованный bean {@link RestClient}
 * с base URL и timeout, настроенными из типизированных свойств.</p>
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
            ExternalRestSystemsProperties properties
    ) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.systemC().connectTimeout());
        requestFactory.setReadTimeout(properties.systemC().readTimeout());

        return builder
                .baseUrl(properties.systemC().baseUrl().toString())
                .requestFactory(requestFactory)
                .build();
    }
}
