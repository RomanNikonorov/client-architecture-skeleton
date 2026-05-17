package me.nikonorov.clients.application;

import java.util.List;

/**
 * Application result returned by the client aggregation use case.
 *
 * <p>The record is transport-neutral and can be mapped to REST responses, gRPC
 * responses, events, or tests without leaking adapter-specific classes into the
 * application layer.</p>
 *
 * @param requestId request correlation identifier copied from the command
 * @param clientId client identifier copied from the command
 * @param segment segment loaded from the local client profile store
 * @param riskScore risk score loaded from the local client profile store
 * @param systemA signal returned by external system A
 * @param systemB signal returned by external system B
 * @param warnings non-fatal aggregation warnings that should be visible to callers
 */
public record ClientAggregationResult(
        String requestId,
        String clientId,
        String segment,
        int riskScore,
        ExternalSignal systemA,
        ExternalSignal systemB,
        List<String> warnings
) {
    /**
     * Normalized signal returned by an external integration.
     *
     * <p>Adapters map their protocol-specific responses and failures into this
     * compact shape so use cases can reason about external signals uniformly.</p>
     *
     * @param source stable integration identifier, for example {@code system-a}
     * @param status integration-level status, for example {@code OK} or {@code UNAVAILABLE}
     * @param value integration-specific payload value or fallback reason
     */
    public record ExternalSignal(String source, String status, String value) {
        /**
         * Creates a standard fallback signal for a non-critical unavailable integration.
         *
         * @param source stable integration identifier
         * @param reason short machine-readable reason, usually an exception type
         * @return normalized unavailable signal
         */
        public static ExternalSignal unavailable(String source, String reason) {
            return new ExternalSignal(source, "UNAVAILABLE", reason);
        }
    }
}
