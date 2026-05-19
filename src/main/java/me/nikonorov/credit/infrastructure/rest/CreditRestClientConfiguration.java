package me.nikonorov.credit.infrastructure.rest;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Инфраструктурная конфигурация REST clients кредитного bounded context.
 */
@Configuration
class CreditRestClientConfiguration {

    /**
     * Создает {@code RestClient}, используемый адаптером pricing.
     *
     * @param builder builder REST-клиента, предоставленный Spring
     * @param properties base URL и timeout-конфигурация pricing
     * @return настроенный REST-клиент для pricing-системы
     */
    @Bean
    RestClient creditPricingRestClient(
            RestClient.Builder builder,
            CreditRestSystemsProperties properties
    ) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.pricing().connectTimeout());
        requestFactory.setReadTimeout(properties.pricing().readTimeout());

        return builder
                .baseUrl(properties.pricing().baseUrl().toString())
                .requestFactory(requestFactory)
                .build();
    }
}
