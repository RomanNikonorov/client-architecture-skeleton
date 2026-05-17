package me.nikonorov.clients.domain;

/**
 * Domain representation of a client profile stored by this service.
 *
 * <p>The record intentionally contains only business data needed by use cases.
 * Persistence concerns such as table names, column lengths, and JPA annotations
 * stay in the infrastructure entity.</p>
 *
 * @param clientId stable client identifier
 * @param segment business segment assigned to the client
 * @param riskScore current risk score used by aggregation scenarios
 */
public record ClientProfile(String clientId, String segment, int riskScore) {
}
