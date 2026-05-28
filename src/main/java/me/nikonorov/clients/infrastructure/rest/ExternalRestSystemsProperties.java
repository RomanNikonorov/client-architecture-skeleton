package me.nikonorov.clients.infrastructure.rest;

import me.nikonorov.http.OutboundRestClientProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Бизнес- и клиентская конфигурация для исходящих REST-интеграций.
 *
 * <p>Каждый REST-адаптер должен получать собственную типизированную конфигурацию
 * вместо прямого чтения сырых значений окружения.</p>
 *
 * @param systemC конфигурация для примерной внешней системы C, доступной через REST
 */
@ConfigurationProperties(prefix = "app.client.external-rest-systems")
public record ExternalRestSystemsProperties(OutboundRestClientProperties systemC) {
}
