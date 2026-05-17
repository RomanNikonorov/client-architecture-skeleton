package me.nikonorov.clients.infrastructure.grpc;

import me.nikonorov.clients.application.usecase.ClientAggregationCommand;
import me.nikonorov.clients.application.usecase.ClientAggregationResult;
import me.nikonorov.clients.application.port.ExternalSystemAClient;
import me.nikonorov.clients.infrastructure.grpc.generated.ClientSignalRequest;
import me.nikonorov.clients.infrastructure.grpc.generated.ExternalSystemAGrpc;
import me.nikonorov.clients.infrastructure.grpc.generated.SystemAResponse;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Исходящий gRPC-адаптер для внешней системы A.
 *
 * <p>Адаптер владеет сгенерированными gRPC-типами, обработкой deadline и маппингом
 * ответа. Прикладные сервисы потребляют только порт {@link ExternalSystemAClient}.</p>
 */
@Component
class ExternalSystemAGrpcClient implements ExternalSystemAClient {

    private final ExternalSystemAGrpc.ExternalSystemABlockingStub stub;
    private final ExternalSystemsProperties properties;

    /**
     * Создает адаптер.
     *
     * @param stub сгенерированный blocking stub, настроенный для system A
     * @param properties бизнес- и deadline-конфигурация внешней системы
     */
    ExternalSystemAGrpcClient(
            ExternalSystemAGrpc.ExternalSystemABlockingStub stub,
            ExternalSystemsProperties properties
    ) {
        this.stub = stub;
        this.properties = properties;
    }

    /**
     * Вызывает system A и маппит ответ в нормализованную модель внешнего сигнала.
     *
     * @param command команда агрегации с идентификаторами запроса и клиента
     * @return нормализованный сигнал от system A
     */
    @Override
    public ClientAggregationResult.ExternalSignal getClientSignal(ClientAggregationCommand command) {
        ClientSignalRequest request = ClientSignalRequest.newBuilder()
                .setRequestId(command.requestId())
                .setClientId(command.clientId())
                .build();

        SystemAResponse response = stub.withDeadlineAfter(properties.systemA().deadline().toMillis(), TimeUnit.MILLISECONDS)
                .getClientSignal(request);

        return new ClientAggregationResult.ExternalSignal(
                "system-a",
                response.getStatus(),
                response.getVerificationLevel()
        );
    }
}
