package me.nikonorov.clients.infrastructure.rest;

import me.nikonorov.clients.application.ClientAggregationCommand;
import me.nikonorov.clients.application.ClientAggregationResult;
import me.nikonorov.clients.application.ExternalSystemCClient;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Example outbound REST adapter for external system C.
 *
 * <p>The adapter demonstrates the standard blocking {@link RestClient} pattern
 * used by this service. It owns URI construction, response mapping, and
 * fallback behavior while exposing only the application port.</p>
 */
@Component
class ExternalSystemCRestClient implements ExternalSystemCClient {

    private final RestClient externalSystemCRestClient;
    private final ExternalRestSystemsProperties properties;

    /**
     * Creates the REST adapter.
     *
     * @param externalSystemCRestClient typed {@code RestClient} bean for system C
     * @param properties REST integration configuration
     */
    ExternalSystemCRestClient(
            RestClient externalSystemCRestClient,
            ExternalRestSystemsProperties properties
    ) {
        this.externalSystemCRestClient = externalSystemCRestClient;
        this.properties = properties;
    }

    /**
     * Calls system C and maps its response to the normalized external signal model.
     *
     * @param command aggregation command containing request and client identifiers
     * @return normalized signal from system C or an unavailable fallback signal
     */
    @Override
    public ClientAggregationResult.ExternalSignal getClientSignal(ClientAggregationCommand command) {
        try {
            SystemCResponse response = externalSystemCRestClient.get()
                    .uri("/clients/{clientId}/signal?requestId={requestId}", command.clientId(), command.requestId())
                    .retrieve()
                    .body(SystemCResponse.class);

            if (response == null) {
                return ClientAggregationResult.ExternalSignal.unavailable("system-c", "EmptyResponse");
            }

            return new ClientAggregationResult.ExternalSignal("system-c", response.status(), response.value());
        } catch (RuntimeException ex) {
            if (properties.systemC().critical()) {
                throw ex;
            }
            return ClientAggregationResult.ExternalSignal.unavailable("system-c", ex.getClass().getSimpleName());
        }
    }

    /**
     * Response DTO owned by the system C REST adapter.
     *
     * @param status integration-level status
     * @param value integration-specific signal value
     */
    record SystemCResponse(String status, String value) {
    }
}
