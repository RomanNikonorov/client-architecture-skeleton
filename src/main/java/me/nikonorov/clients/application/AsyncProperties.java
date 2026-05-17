package me.nikonorov.clients.application;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties that control application-level asynchronous fan-out.
 *
 * <p>The value is intentionally scoped to a single request. It limits how many
 * independent blocking operations a use case may run at the same time for one
 * incoming request, without acting as a global service-wide bulkhead.</p>
 *
 * @param maxParallelTasksPerRequest maximum number of concurrent tasks allowed
 *                                   inside one fan-out scope
 */
@ConfigurationProperties(prefix = "app.async")
public record AsyncProperties(int maxParallelTasksPerRequest) {

    /**
     * Normalizes invalid configuration values.
     *
     * <p>Values below {@code 1} are converted to {@code 1}, because a fan-out
     * scope with no available execution slot would deadlock every request.</p>
     */
    public AsyncProperties {
        if (maxParallelTasksPerRequest < 1) {
            maxParallelTasksPerRequest = 1;
        }
    }
}
