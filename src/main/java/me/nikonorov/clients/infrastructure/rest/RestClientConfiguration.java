package me.nikonorov.clients.infrastructure.rest;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Infrastructure configuration for outbound REST clients.
 *
 * <p>Each external REST system should have a named {@link RestClient} bean with
 * base URL and timeouts configured from typed properties.</p>
 */
@Configuration
class RestClientConfiguration {

    /**
     * Creates the {@code RestClient} used by the system C adapter.
     *
     * @param builder Spring-provided REST client builder
     * @param properties system C base URL and timeout configuration
     * @return configured REST client for external system C
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
