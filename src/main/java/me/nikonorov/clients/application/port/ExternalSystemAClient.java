package me.nikonorov.clients.application.port;

import me.nikonorov.clients.application.usecase.ClientAggregationCommand;
import me.nikonorov.clients.application.usecase.ClientAggregationResult;

/**
 * Прикладной порт для внешней системы A.
 *
 * <p>Прикладной слой зависит от этого интерфейса вместо сгенерированного gRPC stub.
 * Инфраструктурный адаптер владеет протокольным маппингом, deadlines и
 * транспортными исключениями.</p>
 */
public interface ExternalSystemAClient {

    /**
     * Загружает клиентский сигнал из внешней системы A.
     *
     * @param command команда агрегации с идентификаторами запроса и клиента
     * @return нормализованный внешний сигнал для сценария агрегации
     */
    ClientAggregationResult.ExternalSignal getClientSignal(ClientAggregationCommand command);
}
