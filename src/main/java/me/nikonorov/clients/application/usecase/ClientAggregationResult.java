package me.nikonorov.clients.application.usecase;

import java.util.List;

/**
 * Прикладной результат, возвращаемый сценарием агрегации клиента.
 *
 * <p>Запись не зависит от транспорта и может маппиться в REST-ответы,
 * gRPC-ответы, события или тесты без протекания классов конкретных адаптеров в
 * прикладной слой.</p>
 *
 * @param requestId корреляционный идентификатор запроса, скопированный из команды
 * @param clientId идентификатор клиента, скопированный из команды
 * @param segment сегмент, загруженный из локального хранилища профилей клиентов
 * @param riskScore оценка риска, загруженная из локального хранилища профилей клиентов
 * @param systemA сигнал, возвращенный внешней системой A
 * @param systemB сигнал, возвращенный внешней системой B
 * @param warnings некритичные предупреждения агрегации, видимые вызывающей стороне
 */
public record ClientAggregationResult(
        String requestId,
        String clientId,
        String segment,
        int riskScore,
        ExternalSignal systemA,
        ExternalSignal systemB,
        List<String> warnings
) {
    /**
     * Нормализованный сигнал, возвращаемый внешней интеграцией.
     *
     * <p>Адаптеры маппят протокольные ответы и ошибки в эту компактную форму,
     * чтобы сценарии использования единообразно работали с внешними сигналами.</p>
     *
     * @param source стабильный идентификатор интеграции, например {@code system-a}
     * @param status статус уровня интеграции, например {@code OK} или {@code UNAVAILABLE}
     * @param value значение полезной нагрузки интеграции или причина fallback-ответа
     */
    public record ExternalSignal(String source, String status, String value) {
        /**
         * Создает стандартный fallback-сигнал для недоступной некритичной интеграции.
         *
         * @param source стабильный идентификатор интеграции
         * @param reason короткая машинно-читаемая причина, обычно тип исключения
         * @return нормализованный сигнал недоступности
         */
        public static ExternalSignal unavailable(String source, String reason) {
            return new ExternalSignal(source, "UNAVAILABLE", reason);
        }
    }
}
