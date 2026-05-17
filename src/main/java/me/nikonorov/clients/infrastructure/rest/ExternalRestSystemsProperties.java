package me.nikonorov.clients.infrastructure.rest;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;
import java.time.Duration;

/**
 * Business and client configuration for outbound REST integrations.
 *
 * <p>Each REST adapter should receive its own typed configuration instead of
 * reading raw environment values directly.</p>
 *
 * @param systemC configuration for the example REST-backed external system C
 */
@ConfigurationProperties(prefix = "app.external-rest-systems")
public record ExternalRestSystemsProperties(SystemConfig systemC) {

    /**
     * Configuration shared by outbound REST adapters.
     *
     * @param baseUrl base URL used to build the adapter {@code RestClient}
     * @param connectTimeout timeout for establishing the HTTP connection
     * @param readTimeout timeout for reading the HTTP response
     * @param critical whether adapter failures should be propagated
     */
    public record SystemConfig(
            URI baseUrl,
            Duration connectTimeout,
            Duration readTimeout,
            boolean critical
    ) {
    }
}
