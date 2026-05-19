package me.nikonorov.credit.infrastructure.db;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA entity, замапленная на таблицу {@code credit_policy}.
 *
 * <p>Класс package-private, потому что persistence details не должны протекать
 * в application или domain слой кредитного bounded context.</p>
 */
@Entity
@Table(name = "credit_policy")
class CreditPolicyEntity {

    @Id
    @Column(name = "client_id", nullable = false, length = 64)
    private String clientId;

    @Column(name = "max_limit", nullable = false)
    private int maxLimit;

    @Column(name = "blocked", nullable = false)
    private boolean blocked;

    /**
     * Конструктор для JPA.
     */
    protected CreditPolicyEntity() {
    }

    /**
     * @return сохраненный идентификатор клиента
     */
    String getClientId() {
        return clientId;
    }

    /**
     * @return сохраненный максимальный лимит
     */
    int getMaxLimit() {
        return maxLimit;
    }

    /**
     * @return признак блокировки кредитных продуктов
     */
    boolean isBlocked() {
        return blocked;
    }
}
