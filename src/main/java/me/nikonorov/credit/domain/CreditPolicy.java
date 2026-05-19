package me.nikonorov.credit.domain;

/**
 * Доменная кредитная политика клиента.
 *
 * @param clientId стабильный идентификатор клиента
 * @param maxLimit максимальный локально разрешенный лимит
 * @param blocked запрещено ли выдавать кредитные продукты клиенту
 */
public record CreditPolicy(String clientId, int maxLimit, boolean blocked) {
}
