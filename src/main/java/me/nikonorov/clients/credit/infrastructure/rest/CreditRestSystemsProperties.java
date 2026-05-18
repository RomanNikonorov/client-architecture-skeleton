package me.nikonorov.clients.credit.infrastructure.rest;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;
import java.time.Duration;

/**
 * Бизнес- и клиентская конфигурация REST-интеграций кредитного bounded context.
 *
 * @param pricing конфигурация внешней pricing-системы
 */
@ConfigurationProperties(prefix = "app.credit.external-rest-systems")
public record CreditRestSystemsProperties(SystemConfig pricing) {

    /**
     * Конфигурация REST-адаптера.
     *
     * @param baseUrl base URL pricing-системы
     * @param connectTimeout timeout на установку HTTP-соединения
     * @param readTimeout timeout на чтение HTTP-ответа
     * @param critical нужно ли пробрасывать ошибки адаптера
     */
    public record SystemConfig(
            URI baseUrl,
            Duration connectTimeout,
            Duration readTimeout,
            boolean critical
    ) {
    }
}
