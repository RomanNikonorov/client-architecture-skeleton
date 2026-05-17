package me.nikonorov.clients.domain;

/**
 * Domain repository contract for loading client profiles.
 *
 * <p>Application services use this port without knowing whether the data comes
 * from JPA, another database technology, a cache, or a remote service.</p>
 */
public interface ClientProfileRepository {

    /**
     * Finds a client profile by its business identifier.
     *
     * @param clientId client identifier from an application command
     * @return matching client profile
     * @throws RuntimeException when no profile exists; the infrastructure
     *                          adapter chooses the concrete exception type
     */
    ClientProfile findByClientId(String clientId);
}
