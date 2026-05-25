package me.nikonorov.http;

import io.micrometer.observation.ObservationRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Shared baseline configuration для исходящих REST client builders.
 *
 * <p>Bean {@code outboundRestClientBuilder} хранит общие технические настройки
 * Spring HTTP client, включая {@link ObservationRegistry}. Infrastructure
 * configuration конкретной интеграции должна клонировать этот builder через
 * {@link RestClient.Builder#clone()}, а затем задавать только
 * integration-specific {@code baseUrl} и {@code requestFactory}.</p>
 */
@Configuration
class OutboundRestClientConfiguration {

    /**
     * Создает базовый {@code RestClient.Builder} для исходящих интеграций.
     *
     * @param observationRegistry registry для tracing/metrics HTTP client requests
     * @return базовый builder, который клонируют конкретные интеграции
     */
    @Bean("outboundRestClientBuilder")
    RestClient.Builder outboundRestClientBuilder(ObservationRegistry observationRegistry) {
        return RestClient.builder()
                .observationRegistry(observationRegistry);
    }
}
