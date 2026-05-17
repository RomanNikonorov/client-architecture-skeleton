package me.nikonorov.clients.infrastructure.db;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data repository для {@link ClientProfileEntity}.
 *
 * <p>Этот интерфейс является инфраструктурной деталью. Прикладной код должен
 * вместо него использовать {@link me.nikonorov.clients.domain.ClientProfileRepository}.</p>
 */
interface JpaClientProfileRepository extends JpaRepository<ClientProfileEntity, String> {
}
