package me.nikonorov.clients.infrastructure.grpc;

import me.nikonorov.clients.application.ClientAggregationCommand;
import me.nikonorov.clients.application.ClientAggregationResult;
import me.nikonorov.clients.application.ExternalSystemBClient;
import me.nikonorov.clients.infrastructure.grpc.generated.ClientSignalRequest;
import me.nikonorov.clients.infrastructure.grpc.generated.ExternalSystemBGrpc;
import me.nikonorov.clients.infrastructure.grpc.generated.SystemBResponse;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Outbound gRPC adapter for external system B.
 *
 * <p>System B is modeled as an optional integration by default. Runtime
 * failures can be wrapped with a circuit breaker and converted into an
 * {@code UNAVAILABLE} signal unless configuration marks the integration as
 * critical.</p>
 */
@Component
class ExternalSystemBGrpcClient implements ExternalSystemBClient {

    private final ExternalSystemBGrpc.ExternalSystemBBlockingStub stub;
    private final ExternalSystemsProperties properties;
    private final CircuitBreaker circuitBreaker;

    /**
     * Creates the adapter.
     *
     * @param stub generated blocking stub configured for system B
     * @param properties external-system business, deadline, and fallback configuration
     * @param circuitBreakers registry used to obtain the named system B circuit breaker
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
     * Calls system B and applies optional resilience/fallback policy.
     *
     * @param command aggregation command containing request and client identifiers
     * @return normalized signal from system B or an unavailable fallback signal
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
     * Performs the raw gRPC call to system B without fallback handling.
     *
     * @param command aggregation command containing request and client identifiers
     * @return normalized successful signal from system B
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
