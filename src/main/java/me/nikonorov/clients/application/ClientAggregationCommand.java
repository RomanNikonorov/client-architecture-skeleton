package me.nikonorov.clients.application;

/**
 * Application command for the client aggregation scenario.
 *
 * <p>Inbound adapters create this record after transport-specific validation
 * and mapping. Use cases should depend on this application type instead of REST
 * DTOs, gRPC messages, or other adapter-owned request models.</p>
 *
 * @param requestId caller-provided request correlation identifier
 * @param clientId business identifier of the client being aggregated
 */
public record ClientAggregationCommand(String requestId, String clientId) {
}
