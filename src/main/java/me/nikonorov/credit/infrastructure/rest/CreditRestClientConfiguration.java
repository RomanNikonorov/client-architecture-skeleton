package me.nikonorov.credit.infrastructure.rest;

import me.nikonorov.http.PooledRestClientRequestFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Инфраструктурная конфигурация REST clients кредитного bounded context.
 *
 * <p>Каждый {@link RestClient} получает отдельный HTTP connection pool и
 * timeout-настройки из типизированной конфигурации.</p>
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
            @Qualifier("creditPricingRequestFactory")
            HttpComponentsClientHttpRequestFactory requestFactory,
            CreditRestSystemsProperties properties
    ) {
        return builder
                .baseUrl(properties.pricing().baseUrl().toString())
                .requestFactory(requestFactory)
                .build();
    }

    /**
     * Создает request factory с отдельным connection pool для pricing-системы.
     *
     * @param properties timeout и pool-конфигурация pricing
     * @return request factory для {@code creditPricingRestClient}
     */
    @Bean(destroyMethod = "destroy")
    HttpComponentsClientHttpRequestFactory creditPricingRequestFactory(
            CreditRestSystemsProperties properties
    ) {
        CreditRestSystemsProperties.SystemConfig pricing = properties.pricing();
        return PooledRestClientRequestFactory.create(
                pricing.connectTimeout(),
                pricing.readTimeout(),
                pricing.poolSize()
        );
    }
}
