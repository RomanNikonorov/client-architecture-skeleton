package me.nikonorov.clients.infrastructure.db;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA entity mapped to the {@code client_profile} table.
 *
 * <p>This class is package-private because persistence details should not leak
 * into the domain or application layers. The repository adapter maps it to
 * {@link me.nikonorov.clients.domain.ClientProfile}.</p>
 */
@Entity
@Table(name = "client_profile")
class ClientProfileEntity {

    @Id
    @Column(name = "client_id", nullable = false, length = 64)
    private String clientId;

    @Column(name = "segment", nullable = false, length = 64)
    private String segment;

    @Column(name = "risk_score", nullable = false)
    private int riskScore;

    /**
     * JPA constructor.
     */
    protected ClientProfileEntity() {
    }

    /**
     * @return stored client identifier
     */
    String getClientId() {
        return clientId;
    }

    /**
     * @return stored client segment
     */
    String getSegment() {
        return segment;
    }

    /**
     * @return stored client risk score
     */
    int getRiskScore() {
        return riskScore;
    }
}
