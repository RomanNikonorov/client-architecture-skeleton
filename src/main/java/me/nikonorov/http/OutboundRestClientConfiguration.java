package me.nikonorov.http;

import io.micrometer.observation.ObservationRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Shared baseline configuration для исходящих REST clients.
 *
 * <p>Bean {@code outboundRestClient} хранит общие технические настройки
 * Spring HTTP client, включая {@link ObservationRegistry}. Infrastructure
 * configuration конкретной интеграции должна клонировать этот client через
 * {@link RestClient#mutate()}, а затем задавать только integration-specific
 * {@code baseUrl} и {@code requestFactory}.</p>
 */
@Configuration
class OutboundRestClientConfiguration {

    /**
     * Создает базовый {@code RestClient} для исходящих интеграций.
     *
     * @param observationRegistry registry для tracing/metrics HTTP client requests
     * @return базовый REST-клиент, который клонируют конкретные интеграции
     */
    @Bean("outboundRestClient")
    RestClient outboundRestClient(ObservationRegistry observationRegistry) {
        return RestClient.create()
                .mutate()
                .observationRegistry(observationRegistry)
                .build();
    }
}
