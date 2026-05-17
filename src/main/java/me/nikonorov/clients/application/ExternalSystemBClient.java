package me.nikonorov.clients.application;

/**
 * Application port for external system B.
 *
 * <p>Implementations decide whether failures are propagated or converted into
 * an unavailable signal. The use case only consumes the normalized result.</p>
 */
public interface ExternalSystemBClient {

    /**
     * Loads the client signal from external system B.
     *
     * @param command aggregation command containing request and client identifiers
     * @return normalized external signal or an unavailable fallback signal
     */
    ClientAggregationResult.ExternalSignal getClientSignal(ClientAggregationCommand command);
}
