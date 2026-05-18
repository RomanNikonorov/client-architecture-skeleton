package me.nikonorov.clients.client.infrastructure.db;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception, возникающий, когда локальное хранилище профилей не содержит запрошенного клиента.
 *
 * <p>Аннотация {@link ResponseStatus} позволяет простым REST flows возвращать
 * {@code 404 Not Found}. Более крупные сервисы могут заменить это централизованным
 * exception mapper без изменения domain repository port.</p>
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
class ClientProfileNotFoundException extends RuntimeException {

    /**
     * Создает exception для отсутствующего клиента.
     *
     * @param clientId запрошенный идентификатор клиента
     */
    ClientProfileNotFoundException(String clientId) {
        super("Client profile not found: " + clientId);
    }
}
