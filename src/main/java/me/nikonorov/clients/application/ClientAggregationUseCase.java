package me.nikonorov.clients.application;

import me.nikonorov.clients.domain.ClientProfile;
import me.nikonorov.clients.domain.ClientProfileRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Orchestrates the client aggregation scenario.
 *
 * <p>The use case is deliberately transport-neutral. REST and gRPC adapters map
 * their requests into {@link ClientAggregationCommand}, then this class performs
 * the business orchestration by reading the local profile and collecting
 * external signals through application ports.</p>
 *
 * <p>Parallel work is delegated to {@link FanOutExecutor}. This class must not
 * create virtual threads, semaphores, or futures directly.</p>
 */
@Service
public class ClientAggregationUseCase {

    private final ClientProfileRepository profiles;
    private final ExternalSystemAClient systemA;
    private final ExternalSystemBClient systemB;
    private final FanOutExecutor fanOutExecutor;
    private final int maxParallelTasksPerRequest;

    /**
     * Creates the aggregation use case with all required application ports.
     *
     * @param profiles local client profile repository port
     * @param systemA outbound port for required external system A
     * @param systemB outbound port for external system B
     * @param fanOutExecutor central bounded fan-out executor
     * @param asyncProperties fan-out limit configuration
     */
    public ClientAggregationUseCase(
            ClientProfileRepository profiles,
            ExternalSystemAClient systemA,
            ExternalSystemBClient systemB,
            FanOutExecutor fanOutExecutor,
            AsyncProperties asyncProperties
    ) {
        this.profiles = profiles;
        this.systemA = systemA;
        this.systemB = systemB;
        this.fanOutExecutor = fanOutExecutor;
        this.maxParallelTasksPerRequest = asyncProperties.maxParallelTasksPerRequest();
    }

    /**
     * Aggregates local and external data for one client.
     *
     * <p>The profile lookup and both external calls are submitted to the same
     * fan-out scope, so the configured per-request limit controls all parallel
     * work for this command. Non-critical integrations should return normalized
     * {@code UNAVAILABLE} signals from their adapters; this use case converts
     * those signals into caller-visible warnings.</p>
     *
     * @param command validated application command
     * @return aggregated client data and non-fatal warnings
     */
    public ClientAggregationResult aggregate(ClientAggregationCommand command) {
        FanOutExecutor.FanOutScope fanOut = fanOutExecutor.openScope(maxParallelTasksPerRequest);

        FanOutExecutor.FanOutTask<ClientProfile> profileTask = fanOut.submit(
                () -> profiles.findByClientId(command.clientId()));
        FanOutExecutor.FanOutTask<ClientAggregationResult.ExternalSignal> systemATask = fanOut.submit(
                () -> systemA.getClientSignal(command));
        FanOutExecutor.FanOutTask<ClientAggregationResult.ExternalSignal> systemBTask = fanOut.submit(
                () -> systemB.getClientSignal(command));

        ClientProfile profile = profileTask.join();
        ClientAggregationResult.ExternalSignal signalA = systemATask.join();
        ClientAggregationResult.ExternalSignal signalB = systemBTask.join();

        List<String> warnings = new ArrayList<>();
        if ("UNAVAILABLE".equals(signalA.status())) {
            warnings.add("system-a unavailable");
        }
        if ("UNAVAILABLE".equals(signalB.status())) {
            warnings.add("system-b unavailable");
        }

        return new ClientAggregationResult(
                command.requestId(),
                command.clientId(),
                profile.segment(),
                profile.riskScore(),
                signalA,
                signalB,
                warnings
        );
    }
}
