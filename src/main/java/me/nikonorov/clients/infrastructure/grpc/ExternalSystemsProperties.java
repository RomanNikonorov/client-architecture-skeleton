package me.nikonorov.clients.infrastructure.grpc;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Бизнес-конфигурация для исходящих gRPC-интеграций.
 *
 * <p>Адреса транспортных каналов и SSL-настройки остаются в
 * {@code spring.grpc.client.channels.*}. Эти свойства описывают, как сервис
 * должен обрабатывать каждую внешнюю систему на уровне бизнес-адаптера.</p>
 *
 * @param systemA конфигурация для внешней системы A
 * @param systemB конфигурация для внешней системы B
 */
@ConfigurationProperties(prefix = "app.client.external-systems")
public record ExternalSystemsProperties(SystemConfig systemA, SystemConfig systemB) {

    /**
     * Конфигурация, общая для исходящих gRPC-адаптеров.
     *
     * @param channel имя Spring gRPC channel
     * @param deadline deadline одного вызова
     * @param critical нужно ли пробрасывать ошибки адаптера
     * @param circuitBreakerEnabled должен ли адаптер декорировать вызовы circuit breaker
     */
    public record SystemConfig(
            String channel,
            Duration deadline,
            boolean critical,
            boolean circuitBreakerEnabled
    ) {
    }
}
