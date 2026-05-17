package me.nikonorov.clients.api.grpc;

import me.nikonorov.clients.api.grpc.generated.AggregateClientRequest;
import me.nikonorov.clients.api.grpc.generated.AggregateClientResponse;
import me.nikonorov.clients.application.fanout.AsyncProperties;
import me.nikonorov.clients.application.usecase.ClientAggregationResult;
import me.nikonorov.clients.application.usecase.ClientAggregationUseCase;
import me.nikonorov.clients.application.fanout.FanOutExecutor;
import me.nikonorov.clients.domain.ClientProfile;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class ClientAggregationGrpcServiceTest {

    @Test
    void mapsGrpcRequestToUseCaseCommand() {
        ClientAggregationUseCase useCase = new ClientAggregationUseCase(
                clientId -> new ClientProfile(clientId, "premium", 17),
                command -> new ClientAggregationResult.ExternalSignal("system-a", "OK", "verified"),
                command -> new ClientAggregationResult.ExternalSignal("system-b", "OK", "offer-42"),
                maxParallelTasks -> new DirectFanOutScope(),
                new AsyncProperties(3)
        );
        ClientAggregationGrpcService service = new ClientAggregationGrpcService(useCase);
        AtomicReference<AggregateClientResponse> response = new AtomicReference<>();

        service.aggregateClient(AggregateClientRequest.newBuilder()
                .setRequestId("req-1")
                .setClientId("client-001")
                .build(), new CapturingObserver(response));

        assertThat(response.get().getRequestId()).isEqualTo("req-1");
        assertThat(response.get().getClientId()).isEqualTo("client-001");
        assertThat(response.get().getSystemA().getValue()).isEqualTo("verified");
        assertThat(response.get().getSystemB().getValue()).isEqualTo("offer-42");
    }

    private record CapturingObserver(
            AtomicReference<AggregateClientResponse> response
    ) implements StreamObserver<AggregateClientResponse> {

        @Override
        public void onNext(AggregateClientResponse value) {
            response.set(value);
        }

        @Override
        public void onError(Throwable throwable) {
            throw new AssertionError(throwable);
        }

        @Override
        public void onCompleted() {
        }
    }

    private static class DirectFanOutScope implements FanOutExecutor.FanOutScope {

        @Override
        public <T> FanOutExecutor.FanOutTask<T> submit(java.util.function.Supplier<T> supplier) {
            return supplier::get;
        }
    }
}
