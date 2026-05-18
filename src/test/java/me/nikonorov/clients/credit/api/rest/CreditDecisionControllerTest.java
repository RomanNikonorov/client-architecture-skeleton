package me.nikonorov.clients.credit.api.rest;

import me.nikonorov.clients.application.fanout.AsyncProperties;
import me.nikonorov.clients.application.fanout.FanOutExecutor;
import me.nikonorov.clients.credit.application.usecase.CreditDecisionResult;
import me.nikonorov.clients.credit.application.usecase.CreditDecisionUseCase;
import me.nikonorov.clients.credit.domain.CreditPolicy;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CreditDecisionControllerTest {

    @Test
    void mapsRestRequestToCreditUseCaseCommand() {
        CreditDecisionUseCase useCase = new CreditDecisionUseCase(
                clientId -> new CreditPolicy(clientId, 500000, false),
                command -> new CreditDecisionResult.ScoringAssessment("credit-scoring", "OK", 720, 350000),
                command -> new CreditDecisionResult.PricingOffer("credit-pricing", "OK", "prime", 1490),
                maxParallelTasks -> new DirectFanOutScope(),
                new AsyncProperties(3)
        );
        CreditDecisionController controller = new CreditDecisionController(useCase);

        CreditDecisionResult result = controller.decide(
                new CreditDecisionController.CreditDecisionRequest("req-1", "client-001", 300000)
        ).getBody();

        assertThat(result).isNotNull();
        assertThat(result.requestId()).isEqualTo("req-1");
        assertThat(result.clientId()).isEqualTo("client-001");
        assertThat(result.approved()).isTrue();
    }

    private static class DirectFanOutScope implements FanOutExecutor.FanOutScope {

        @Override
        public <T> FanOutExecutor.FanOutTask<T> submit(java.util.function.Supplier<T> supplier) {
            return supplier::get;
        }
    }
}
