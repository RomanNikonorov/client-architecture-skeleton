package me.nikonorov.clients.client.infrastructure.db;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA entity, замапленная на таблицу {@code client_profile}.
 *
 * <p>Класс package-private, потому что persistence details не должны протекать
 * в доменный или прикладной слои. Адаптер репозитория маппит его в
 * {@link me.nikonorov.clients.client.domain.ClientProfile}.</p>
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
     * Конструктор для JPA.
     */
    protected ClientProfileEntity() {
    }

    /**
     * @return сохраненный идентификатор клиента
     */
    String getClientId() {
        return clientId;
    }

    /**
     * @return сохраненный сегмент клиента
     */
    String getSegment() {
        return segment;
    }

    /**
     * @return сохраненная оценка риска клиента
     */
    int getRiskScore() {
        return riskScore;
    }
}
