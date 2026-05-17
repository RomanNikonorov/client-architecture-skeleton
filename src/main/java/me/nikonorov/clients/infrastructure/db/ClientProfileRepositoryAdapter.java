package me.nikonorov.clients.infrastructure.db;

import me.nikonorov.clients.domain.ClientProfile;
import me.nikonorov.clients.domain.ClientProfileRepository;
import org.springframework.stereotype.Repository;

/**
 * JPA-backed реализация domain-порта репозитория профилей клиентов.
 *
 * <p>Адаптер переводит persistence-сущности и семантику Spring Data в
 * доменный контракт {@link ClientProfileRepository}.</p>
 */
@Repository
class ClientProfileRepositoryAdapter implements ClientProfileRepository {

    private final JpaClientProfileRepository repository;

    /**
     * Создает адаптер репозитория.
     *
     * @param repository Spring Data JPA repository для сущностей профиля
     */
    ClientProfileRepositoryAdapter(JpaClientProfileRepository repository) {
        this.repository = repository;
    }

    /**
     * Загружает и маппит сущность профиля по идентификатору клиента.
     *
     * @param clientId запрошенный идентификатор клиента
     * @return замапленный доменный профиль
     * @throws ClientProfileNotFoundException если профиль не существует
     */
    @Override
    public ClientProfile findByClientId(String clientId) {
        return repository.findById(clientId)
                .map(entity -> new ClientProfile(entity.getClientId(), entity.getSegment(), entity.getRiskScore()))
                .orElseThrow(() -> new ClientProfileNotFoundException(clientId));
    }
}
