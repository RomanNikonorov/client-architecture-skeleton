package me.nikonorov.clients.application;

/**
 * Application port for external system A.
 *
 * <p>The application layer depends on this interface instead of the generated
 * gRPC stub. The infrastructure adapter owns protocol mapping, deadlines, and
 * transport exceptions.</p>
 */
public interface ExternalSystemAClient {

    /**
     * Loads the client signal from external system A.
     *
     * @param command aggregation command containing request and client identifiers
     * @return normalized external signal used by the aggregation use case
     */
    ClientAggregationResult.ExternalSignal getClientSignal(ClientAggregationCommand command);
}
