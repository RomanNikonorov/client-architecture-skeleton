package me.nikonorov.clients.infrastructure.grpc;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Business-level configuration for outbound gRPC integrations.
 *
 * <p>Transport channel addresses and SSL settings remain in
 * {@code spring.grpc.client.channels.*}. These properties describe how the
 * service should treat each external system at the business adapter level.</p>
 *
 * @param systemA configuration for external system A
 * @param systemB configuration for external system B
 */
@ConfigurationProperties(prefix = "app.external-systems")
public record ExternalSystemsProperties(SystemConfig systemA, SystemConfig systemB) {

    /**
     * Configuration shared by outbound gRPC adapters.
     *
     * @param channel Spring gRPC channel name
     * @param deadline per-call deadline
     * @param critical whether adapter failures should be propagated
     * @param circuitBreakerEnabled whether the adapter should decorate calls with a circuit breaker
     */
    public record SystemConfig(
            String channel,
            Duration deadline,
            boolean critical,
            boolean circuitBreakerEnabled
    ) {
    }
}
