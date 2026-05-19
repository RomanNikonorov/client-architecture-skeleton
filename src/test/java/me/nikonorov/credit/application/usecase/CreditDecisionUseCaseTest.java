package me.nikonorov.credit.application.usecase;

import me.nikonorov.fanout.AsyncProperties;
import me.nikonorov.fanout.FanOutExecutor;
import me.nikonorov.credit.application.port.CreditPricingClient;
import me.nikonorov.credit.application.port.CreditScoringClient;
import me.nikonorov.credit.domain.CreditPolicy;
import me.nikonorov.credit.domain.CreditPolicyRepository;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CreditDecisionUseCaseTest {

    @Test
    void evaluatesDecisionFromLocalPolicyAndExternalSystems() {
        CreditPolicyRepository policies = clientId -> new CreditPolicy(clientId, 500000, false);
        CreditScoringClient scoring = command -> new CreditDecisionResult.ScoringAssessment(
                "credit-scoring",
                "OK",
                720,
                350000);
        CreditPricingClient pricing = command -> new CreditDecisionResult.PricingOffer(
                "credit-pricing",
                "OK",
                "prime",
                1490);
        FanOutExecutor fanOutExecutor = maxParallelTasks -> new DirectFanOutScope();
        CreditDecisionUseCase useCase = new CreditDecisionUseCase(
                policies,
                scoring,
                pricing,
                fanOutExecutor,
                new AsyncProperties(3)
        );

        CreditDecisionResult result = useCase.evaluate(new CreditDecisionCommand("req-1", "client-001", 300000));

        assertThat(result.approved()).isTrue();
        assertThat(result.approvedLimit()).isEqualTo(350000);
        assertThat(result.ratePlan()).isEqualTo("prime");
        assertThat(result.warnings()).isEmpty();
    }

    private static class DirectFanOutScope implements FanOutExecutor.FanOutScope {

        @Override
        public <T> FanOutExecutor.FanOutTask<T> submit(java.util.function.Supplier<T> supplier) {
            return supplier::get;
        }
    }
}
