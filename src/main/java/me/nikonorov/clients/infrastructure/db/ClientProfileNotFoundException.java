package me.nikonorov.clients.infrastructure.db;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception raised when the local client profile store has no requested client.
 *
 * <p>The {@link ResponseStatus} annotation lets simple REST flows return
 * {@code 404 Not Found}. Larger services may replace this with a centralized
 * exception mapper without changing the domain repository port.</p>
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
class ClientProfileNotFoundException extends RuntimeException {

    /**
     * Creates the exception for a missing client.
     *
     * @param clientId requested client identifier
     */
    ClientProfileNotFoundException(String clientId) {
        super("Client profile not found: " + clientId);
    }
}
