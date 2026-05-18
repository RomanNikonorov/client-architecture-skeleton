package me.nikonorov.clients.client.api.rest;

import me.nikonorov.clients.application.fanout.AsyncProperties;
import me.nikonorov.clients.client.application.usecase.ClientAggregationResult;
import me.nikonorov.clients.client.application.usecase.ClientAggregationUseCase;
import me.nikonorov.clients.application.fanout.FanOutExecutor;
import me.nikonorov.clients.client.domain.ClientProfile;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ClientAggregationControllerTest {

    @Test
    void mapsRestRequestToUseCaseCommand() {
        ClientAggregationUseCase useCase = new ClientAggregationUseCase(
                clientId -> new ClientProfile(clientId, "premium", 17),
                command -> new ClientAggregationResult.ExternalSignal("system-a", "OK", "verified"),
                command -> new ClientAggregationResult.ExternalSignal("system-b", "OK", "offer-42"),
                maxParallelTasks -> new DirectFanOutScope(),
                new AsyncProperties(3)
        );
        ClientAggregationController controller = new ClientAggregationController(useCase);

        ClientAggregationResult result = controller.aggregate(
                new ClientAggregationController.ClientAggregationRequest("req-1", "client-001")
        ).getBody();

        assertThat(result).isNotNull();
        assertThat(result.requestId()).isEqualTo("req-1");
        assertThat(result.clientId()).isEqualTo("client-001");
        assertThat(result.segment()).isEqualTo("premium");
    }

    private static class DirectFanOutScope implements FanOutExecutor.FanOutScope {

        @Override
        public <T> FanOutExecutor.FanOutTask<T> submit(java.util.function.Supplier<T> supplier) {
            return supplier::get;
        }
    }
}
