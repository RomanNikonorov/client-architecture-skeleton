package me.nikonorov.credit.infrastructure.rest;

import me.nikonorov.http.OutboundRestClientProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;

/**
 * Бизнес- и клиентская конфигурация REST-интеграций кредитного bounded context.
 *
 * @param pricing конфигурация внешней pricing-системы
 */
@ConfigurationProperties(prefix = "app.credit.external-rest-systems")
public record CreditRestSystemsProperties(OutboundRestClientProperties pricing) {

    private static final URI DEFAULT_PRICING_BASE_URL = URI.create("http://localhost:9084");

    public CreditRestSystemsProperties {
        if (pricing == null) {
            pricing = OutboundRestClientProperties.withDefaultHttpParameters(DEFAULT_PRICING_BASE_URL);
        } else {
            pricing = pricing.withDefaultBaseUrl(DEFAULT_PRICING_BASE_URL);
        }
    }
}
