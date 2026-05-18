package me.nikonorov.clients.credit.infrastructure.db;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data repository для {@link CreditPolicyEntity}.
 */
interface JpaCreditPolicyRepository extends JpaRepository<CreditPolicyEntity, String> {
}
