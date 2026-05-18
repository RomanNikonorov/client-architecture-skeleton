package me.nikonorov.clients.credit.infrastructure.grpc;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Бизнес-конфигурация исходящих gRPC-интеграций кредитного bounded context.
 *
 * @param scoring конфигурация внешней scoring-системы
 */
@ConfigurationProperties(prefix = "app.credit.external-systems")
public record CreditExternalSystemsProperties(SystemConfig scoring) {

    /**
     * Конфигурация исходящего gRPC-адаптера.
     *
     * @param channel имя Spring gRPC channel
     * @param deadline deadline одного вызова
     * @param critical нужно ли пробрасывать ошибки адаптера
     */
    public record SystemConfig(String channel, Duration deadline, boolean critical) {
    }
}
