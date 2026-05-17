package me.nikonorov.clients.api.grpc;

import me.nikonorov.clients.application.ClientAggregationCommand;
import me.nikonorov.clients.application.ClientAggregationResult;
import me.nikonorov.clients.application.ClientAggregationUseCase;
import me.nikonorov.clients.api.grpc.generated.AggregateClientRequest;
import me.nikonorov.clients.api.grpc.generated.AggregateClientResponse;
import me.nikonorov.clients.api.grpc.generated.AggregatedExternalSignal;
import me.nikonorov.clients.api.grpc.generated.ClientAggregationApiGrpc;
import io.grpc.stub.StreamObserver;
import org.springframework.grpc.server.service.GrpcService;

/**
 * gRPC inbound adapter for the client aggregation API.
 *
 * <p>The service maps generated gRPC request and response messages at the API
 * boundary. Business orchestration remains in {@link ClientAggregationUseCase},
 * allowing REST and gRPC endpoints to share the same application flow.</p>
 */
@GrpcService
class ClientAggregationGrpcService extends ClientAggregationApiGrpc.ClientAggregationApiImplBase {

    private final ClientAggregationUseCase useCase;

    /**
     * Creates the gRPC service.
     *
     * @param useCase application use case that owns aggregation orchestration
     */
    ClientAggregationGrpcService(ClientAggregationUseCase useCase) {
        this.useCase = useCase;
    }

    /**
     * Handles the unary {@code AggregateClient} gRPC method.
     *
     * @param request generated gRPC request message
     * @param responseObserver gRPC response observer used to emit the result
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
     * Maps the transport-neutral application result to the generated gRPC response.
     *
     * @param result application aggregation result
     * @return generated gRPC response message
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
     * Maps one normalized external signal to its gRPC representation.
     *
     * @param signal application-level external signal
     * @return generated gRPC signal message
     */
    private AggregatedExternalSignal toSignal(ClientAggregationResult.ExternalSignal signal) {
        return AggregatedExternalSignal.newBuilder()
                .setSource(signal.source())
                .setStatus(signal.status())
                .setValue(signal.value())
                .build();
    }
}
