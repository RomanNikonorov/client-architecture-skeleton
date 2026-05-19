package me.nikonorov.clients.infrastructure.grpc;

import me.nikonorov.clients.application.usecase.ClientAggregationCommand;
import me.nikonorov.clients.application.usecase.ClientAggregationResult;
import me.nikonorov.clients.application.port.ExternalSystemBClient;
import me.nikonorov.clients.infrastructure.grpc.generated.ClientSignalRequest;
import me.nikonorov.clients.infrastructure.grpc.generated.ExternalSystemBGrpc;
import me.nikonorov.clients.infrastructure.grpc.generated.SystemBResponse;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Исходящий gRPC-адаптер для внешней системы B.
 *
 * <p>System B по умолчанию смоделирована как необязательная интеграция. Runtime
 * runtime-ошибки могут быть обернуты circuit breaker и преобразованы в сигнал
 * {@code UNAVAILABLE}, если конфигурация не помечает интеграцию как критичную.</p>
 */
@Component
class ExternalSystemBGrpcClient implements ExternalSystemBClient {

    private final ExternalSystemBGrpc.ExternalSystemBBlockingStub stub;
    private final ExternalSystemsProperties properties;
    private final CircuitBreaker circuitBreaker;

    /**
     * Создает адаптер.
     *
     * @param stub сгенерированный blocking stub, настроенный для system B
     * @param properties бизнес-, deadline- и fallback-конфигурация внешней системы
     * @param circuitBreakers registry для получения именованного circuit breaker system B
     */
    ExternalSystemBGrpcClient(
            ExternalSystemBGrpc.ExternalSystemBBlockingStub stub,
            ExternalSystemsProperties properties,
            CircuitBreakerRegistry circuitBreakers
    ) {
        this.stub = stub;
        this.properties = properties;
        this.circuitBreaker = circuitBreakers.circuitBreaker("external-system-b");
    }

    /**
     * Вызывает system B и применяет необязательную resilience/fallback policy.
     *
     * @param command команда агрегации с идентификаторами запроса и клиента
     * @return нормализованный сигнал от system B или fallback-сигнал о недоступности
     */
    @Override
    public ClientAggregationResult.ExternalSignal getClientSignal(ClientAggregationCommand command) {
        Supplier<ClientAggregationResult.ExternalSignal> call = () -> callSystemB(command);

        if (properties.systemB().circuitBreakerEnabled()) {
            call = CircuitBreaker.decorateSupplier(circuitBreaker, call);
        }

        try {
            return call.get();
        } catch (RuntimeException ex) {
            if (properties.systemB().critical()) {
                throw ex;
            }
            return ClientAggregationResult.ExternalSignal.unavailable("system-b", ex.getClass().getSimpleName());
        }
    }

    /**
     * Выполняет raw gRPC call к system B без fallback handling.
     *
     * @param command команда агрегации с идентификаторами запроса и клиента
     * @return нормализованный успешный сигнал от system B
     */
    private ClientAggregationResult.ExternalSignal callSystemB(ClientAggregationCommand command) {
        ClientSignalRequest request = ClientSignalRequest.newBuilder()
                .setRequestId(command.requestId())
                .setClientId(command.clientId())
                .build();

        SystemBResponse response = stub.withDeadlineAfter(properties.systemB().deadline().toMillis(), TimeUnit.MILLISECONDS)
                .getClientOffer(request);

        return new ClientAggregationResult.ExternalSignal(
                "system-b",
                response.getStatus(),
                response.getOfferCode()
        );
    }
}
