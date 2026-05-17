package me.nikonorov.clients.infrastructure.rest;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;
import java.time.Duration;

/**
 * Бизнес- и клиентская конфигурация для исходящих REST-интеграций.
 *
 * <p>Каждый REST-адаптер должен получать собственную типизированную конфигурацию
 * вместо прямого чтения сырых значений окружения.</p>
 *
 * @param systemC конфигурация для примерной внешней системы C, доступной через REST
 */
@ConfigurationProperties(prefix = "app.external-rest-systems")
public record ExternalRestSystemsProperties(SystemConfig systemC) {

    /**
     * Конфигурация, общая для исходящих REST-адаптеров.
     *
     * @param baseUrl base URL для построения адаптера {@code RestClient}
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
