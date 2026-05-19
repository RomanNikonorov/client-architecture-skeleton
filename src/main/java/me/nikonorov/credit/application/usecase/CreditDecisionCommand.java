package me.nikonorov.credit.application.usecase;

/**
 * Прикладная команда кредитного решения.
 *
 * @param requestId корреляционный идентификатор запроса
 * @param clientId бизнес-идентификатор клиента
 * @param requestedAmount запрошенная сумма кредитного продукта
 */
public record CreditDecisionCommand(String requestId, String clientId, int requestedAmount) {
}
