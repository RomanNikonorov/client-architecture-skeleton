package me.nikonorov.clients.infrastructure.db;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data repository for {@link ClientProfileEntity}.
 *
 * <p>This interface is an infrastructure detail. Application code must use
 * {@link me.nikonorov.clients.domain.ClientProfileRepository} instead.</p>
 */
interface JpaClientProfileRepository extends JpaRepository<ClientProfileEntity, String> {
}
