package me.nikonorov.clients.credit.domain;

/**
 * Доменный контракт чтения кредитной политики клиента.
 */
public interface CreditPolicyRepository {

    /**
     * Загружает кредитную политику по идентификатору клиента.
     *
     * @param clientId запрошенный идентификатор клиента
     * @return доменная кредитная политика
     */
    CreditPolicy findByClientId(String clientId);
}
