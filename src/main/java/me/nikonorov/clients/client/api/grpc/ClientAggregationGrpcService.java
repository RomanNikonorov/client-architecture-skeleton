package me.nikonorov.clients.client.api.grpc;

import me.nikonorov.clients.client.application.usecase.ClientAggregationCommand;
import me.nikonorov.clients.client.application.usecase.ClientAggregationResult;
import me.nikonorov.clients.client.application.usecase.ClientAggregationUseCase;
import me.nikonorov.clients.client.api.grpc.generated.AggregateClientRequest;
import me.nikonorov.clients.client.api.grpc.generated.AggregateClientResponse;
import me.nikonorov.clients.client.api.grpc.generated.AggregatedExternalSignal;
import me.nikonorov.clients.client.api.grpc.generated.ClientAggregationApiGrpc;
import io.grpc.stub.StreamObserver;
import org.springframework.grpc.server.service.GrpcService;

/**
 * Входной gRPC-адаптер для API агрегации клиента.
 *
 * <p>Service маппит сгенерированные gRPC-сообщения запроса и ответа на границе API.
 * Бизнес-оркестрация остается в {@link ClientAggregationUseCase}, что позволяет
 * REST и gRPC endpoints использовать один прикладной flow.</p>
 */
@GrpcService
class ClientAggregationGrpcService extends ClientAggregationApiGrpc.ClientAggregationApiImplBase {

    private final ClientAggregationUseCase useCase;

    /**
     * Создает gRPC service.
     *
     * @param useCase прикладной сценарий, владеющий оркестрацией агрегации
     */
    ClientAggregationGrpcService(ClientAggregationUseCase useCase) {
        this.useCase = useCase;
    }

    /**
     * Обрабатывает unary gRPC method {@code AggregateClient}.
     *
     * @param request сгенерированное gRPC-сообщение запроса
     * @param responseObserver gRPC response observer для отправки результата
     */
    @Override
    public void aggregateClient(
            AggregateClientRequest request,
            StreamObserver<AggregateClientResponse> responseObserver
    ) {
        ClientAggregationResult result = useCase.aggregate(
                new ClientAggregationCommand(request.getRequestId(), request.getClientId()));

        responseObserver.onNext(toResponse(result));
        responseObserver.onCompleted();
    }

    /**
     * Маппит транспортно-независимый прикладной результат в сгенерированный gRPC-ответ.
     *
     * @param result прикладной результат агрегации
     * @return сгенерированное gRPC-сообщение ответа
     */
    private AggregateClientResponse toResponse(ClientAggregationResult result) {
        return AggregateClientResponse.newBuilder()
                .setRequestId(result.requestId())
                .setClientId(result.clientId())
                .setSegment(result.segment())
                .setRiskScore(result.riskScore())
                .setSystemA(toSignal(result.systemA()))
                .setSystemB(toSignal(result.systemB()))
                .addAllWarnings(result.warnings())
                .build();
    }

    /**
     * Маппит один нормализованный внешний сигнал в его gRPC-представление.
     *
     * @param signal прикладной внешний сигнал
     * @return сгенерированное gRPC signal message
     */
    private AggregatedExternalSignal toSignal(ClientAggregationResult.ExternalSignal signal) {
        return AggregatedExternalSignal.newBuilder()
                .setSource(signal.source())
                .setStatus(signal.status())
                .setValue(signal.value())
                .build();
    }
}
