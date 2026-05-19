package me.nikonorov.credit.infrastructure.db;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception, возникающий, когда локальное хранилище не содержит кредитной политики клиента.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
class CreditPolicyNotFoundException extends RuntimeException {

    /**
     * Создает exception для отсутствующей кредитной политики.
     *
     * @param clientId запрошенный идентификатор клиента
     */
    CreditPolicyNotFoundException(String clientId) {
        super("Credit policy not found: " + clientId);
    }
}
