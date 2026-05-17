package me.nikonorov.clients.application;

import me.nikonorov.clients.domain.ClientProfile;
import me.nikonorov.clients.domain.ClientProfileRepository;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ClientAggregationUseCaseTest {

    @Test
    void aggregatesDatabaseAndTwoExternalSystemResults() {
        ClientProfileRepository profiles = clientId -> new ClientProfile(clientId, "premium", 17);
        ExternalSystemAClient systemA = command -> new ClientAggregationResult.ExternalSignal("system-a", "OK", "verified");
        ExternalSystemBClient systemB = command -> new ClientAggregationResult.ExternalSignal("system-b", "OK", "offer-42");
        FanOutExecutor fanOutExecutor = maxParallelTasks -> new DirectFanOutScope();

        ClientAggregationUseCase useCase = new ClientAggregationUseCase(
                profiles,
                systemA,
                systemB,
                fanOutExecutor,
                new AsyncProperties(3)
        );

        ClientAggregationResult result = useCase.aggregate(new ClientAggregationCommand("req-1", "client-001"));

        assertThat(result.segment()).isEqualTo("premium");
        assertThat(result.systemA().value()).isEqualTo("verified");
        assertThat(result.systemB().value()).isEqualTo("offer-42");
        assertThat(result.warnings()).isEmpty();
    }

    private static class DirectFanOutScope implements FanOutExecutor.FanOutScope {

        @Override
        public <T> FanOutExecutor.FanOutTask<T> submit(java.util.function.Supplier<T> supplier) {
            return supplier::get;
        }
    }
}
