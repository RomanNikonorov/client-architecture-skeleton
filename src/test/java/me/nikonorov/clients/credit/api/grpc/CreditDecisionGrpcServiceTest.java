package me.nikonorov.clients.credit.api.grpc;

import io.grpc.stub.StreamObserver;
import me.nikonorov.clients.application.fanout.AsyncProperties;
import me.nikonorov.clients.application.fanout.FanOutExecutor;
import me.nikonorov.clients.credit.api.grpc.generated.CreditDecisionRequest;
import me.nikonorov.clients.credit.api.grpc.generated.CreditDecisionResponse;
import me.nikonorov.clients.credit.application.usecase.CreditDecisionResult;
import me.nikonorov.clients.credit.application.usecase.CreditDecisionUseCase;
import me.nikonorov.clients.credit.domain.CreditPolicy;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class CreditDecisionGrpcServiceTest {

    @Test
    void mapsGrpcRequestToCreditUseCaseCommand() {
        CreditDecisionUseCase useCase = new CreditDecisionUseCase(
                clientId -> new CreditPolicy(clientId, 500000, false),
                command -> new CreditDecisionResult.ScoringAssessment("credit-scoring", "OK", 720, 350000),
                command -> new CreditDecisionResult.PricingOffer("credit-pricing", "OK", "prime", 1490),
                maxParallelTasks -> new DirectFanOutScope(),
                new AsyncProperties(3)
        );
        CreditDecisionGrpcService service = new CreditDecisionGrpcService(useCase);
        AtomicReference<CreditDecisionResponse> response = new AtomicReference<>();

        service.decideCredit(CreditDecisionRequest.newBuilder()
                .setRequestId("req-1")
                .setClientId("client-001")
                .setRequestedAmount(300000)
                .build(), new CapturingObserver(response));

        assertThat(response.get().getRequestId()).isEqualTo("req-1");
        assertThat(response.get().getClientId()).isEqualTo("client-001");
        assertThat(response.get().getApproved()).isTrue();
        assertThat(response.get().getScoring().getScore()).isEqualTo(720);
        assertThat(response.get().getPricing().getRatePlan()).isEqualTo("prime");
    }

    private record CapturingObserver(
            AtomicReference<CreditDecisionResponse> response
    ) implements StreamObserver<CreditDecisionResponse> {

        @Override
        public void onNext(CreditDecisionResponse value) {
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
