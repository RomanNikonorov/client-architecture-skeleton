package me.nikonorov.clients.infrastructure.grpc;

import me.nikonorov.clients.application.ClientAggregationCommand;
import me.nikonorov.clients.application.ClientAggregationResult;
import me.nikonorov.clients.application.ExternalSystemAClient;
import me.nikonorov.clients.infrastructure.grpc.generated.ClientSignalRequest;
import me.nikonorov.clients.infrastructure.grpc.generated.ExternalSystemAGrpc;
import me.nikonorov.clients.infrastructure.grpc.generated.SystemAResponse;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Outbound gRPC adapter for external system A.
 *
 * <p>The adapter owns generated gRPC types, deadline handling, and response
 * mapping. Application services consume only the {@link ExternalSystemAClient}
 * port.</p>
 */
@Component
class ExternalSystemAGrpcClient implements ExternalSystemAClient {

    private final ExternalSystemAGrpc.ExternalSystemABlockingStub stub;
    private final ExternalSystemsProperties properties;

    /**
     * Creates the adapter.
     *
     * @param stub generated blocking stub configured for system A
     * @param properties external-system business and deadline configuration
     */
    ExternalSystemAGrpcClient(
            ExternalSystemAGrpc.ExternalSystemABlockingStub stub,
            ExternalSystemsProperties properties
    ) {
        this.stub = stub;
        this.properties = properties;
    }

    /**
     * Calls system A and maps its response to the normalized external signal model.
     *
     * @param command aggregation command containing request and client identifiers
     * @return normalized signal from system A
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
