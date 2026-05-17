package me.nikonorov.clients.infrastructure.db;

import me.nikonorov.clients.domain.ClientProfile;
import me.nikonorov.clients.domain.ClientProfileRepository;
import org.springframework.stereotype.Repository;

/**
 * JPA-backed implementation of the domain client profile repository port.
 *
 * <p>The adapter translates persistence entities and Spring Data semantics into
 * the domain-facing {@link ClientProfileRepository} contract.</p>
 */
@Repository
class ClientProfileRepositoryAdapter implements ClientProfileRepository {

    private final JpaClientProfileRepository repository;

    /**
     * Creates the repository adapter.
     *
     * @param repository Spring Data JPA repository for profile entities
     */
    ClientProfileRepositoryAdapter(JpaClientProfileRepository repository) {
        this.repository = repository;
    }

    /**
     * Loads and maps a profile entity by client identifier.
     *
     * @param clientId requested client identifier
     * @return mapped domain profile
     * @throws ClientProfileNotFoundException when the profile does not exist
     */
    @Override
    public ClientProfile findByClientId(String clientId) {
        return repository.findById(clientId)
                .map(entity -> new ClientProfile(entity.getClientId(), entity.getSegment(), entity.getRiskScore()))
                .orElseThrow(() -> new ClientProfileNotFoundException(clientId));
    }
}
