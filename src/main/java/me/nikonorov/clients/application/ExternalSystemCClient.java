package me.nikonorov.clients.application;

/**
 * Application port that demonstrates the standard outbound REST integration shape.
 *
 * <p>The current aggregation use case does not consume this port yet. It exists
 * as a template for future REST-backed external systems: application code
 * depends on a port, while infrastructure owns {@code RestClient} details.</p>
 */
public interface ExternalSystemCClient {

    /**
     * Loads the client signal from external system C.
     *
     * @param command aggregation command containing request and client identifiers
     * @return normalized external signal or an unavailable fallback signal
     */
    ClientAggregationResult.ExternalSignal getClientSignal(ClientAggregationCommand command);
}
