package me.nikonorov.clients.application.port;

import me.nikonorov.clients.application.usecase.ClientAggregationCommand;
import me.nikonorov.clients.application.usecase.ClientAggregationResult;

/**
 * Прикладной порт для внешней системы B.
 *
 * <p>Реализации решают, пробрасывать ошибки или преобразовывать их в
 * unavailable-сигнал. Сценарий использования потребляет только нормализованный результат.</p>
 */
public interface ExternalSystemBClient {

    /**
     * Загружает клиентский сигнал из внешней системы B.
     *
     * @param command команда агрегации с идентификаторами запроса и клиента
     * @return нормализованный внешний сигнал или fallback-сигнал о недоступности
     */
    ClientAggregationResult.ExternalSignal getClientSignal(ClientAggregationCommand command);
}
