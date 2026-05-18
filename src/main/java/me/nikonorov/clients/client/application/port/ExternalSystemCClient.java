package me.nikonorov.clients.client.application.port;

import me.nikonorov.clients.client.application.usecase.ClientAggregationCommand;
import me.nikonorov.clients.client.application.usecase.ClientAggregationResult;

/**
 * Прикладной порт, демонстрирующий стандартную форму исходящей REST-интеграции.
 *
 * <p>Текущий сценарий агрегации пока не использует этот порт. Он существует как
 * шаблон для будущих внешних систем, доступных через REST: прикладной код зависит от
 * порта, а инфраструктура владеет деталями {@code RestClient}.</p>
 */
public interface ExternalSystemCClient {

    /**
     * Загружает клиентский сигнал из внешней системы C.
     *
     * @param command команда агрегации с идентификаторами запроса и клиента
     * @return нормализованный внешний сигнал или fallback-сигнал о недоступности
     */
    ClientAggregationResult.ExternalSignal getClientSignal(ClientAggregationCommand command);
}
