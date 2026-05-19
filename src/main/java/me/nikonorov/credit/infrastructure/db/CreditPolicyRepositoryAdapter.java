package me.nikonorov.credit.infrastructure.db;

import me.nikonorov.credit.domain.CreditPolicy;
import me.nikonorov.credit.domain.CreditPolicyRepository;
import org.springframework.stereotype.Repository;

/**
 * JPA-backed реализация доменного репозитория кредитных политик.
 */
@Repository
class CreditPolicyRepositoryAdapter implements CreditPolicyRepository {

    private final JpaCreditPolicyRepository repository;

    /**
     * Создает адаптер репозитория.
     *
     * @param repository Spring Data JPA repository для сущностей кредитной политики
     */
    CreditPolicyRepositoryAdapter(JpaCreditPolicyRepository repository) {
        this.repository = repository;
    }

    /**
     * Загружает и маппит сущность кредитной политики по идентификатору клиента.
     *
     * @param clientId запрошенный идентификатор клиента
     * @return доменная кредитная политика
     */
    @Override
    public CreditPolicy findByClientId(String clientId) {
        return repository.findById(clientId)
                .map(entity -> new CreditPolicy(entity.getClientId(), entity.getMaxLimit(), entity.isBlocked()))
                .orElseThrow(() -> new CreditPolicyNotFoundException(clientId));
    }
}
