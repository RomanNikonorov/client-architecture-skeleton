package me.nikonorov.clients.client.infrastructure.rest;

import me.nikonorov.clients.client.application.usecase.ClientAggregationCommand;
import me.nikonorov.clients.client.application.usecase.ClientAggregationResult;
import me.nikonorov.clients.client.application.port.ExternalSystemCClient;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Пример исходящего REST-адаптера для внешней системы C.
 *
 * <p>Адаптер демонстрирует стандартный блокирующий pattern с {@link RestClient},
 * используемый этим сервисом. Он владеет построением URI, маппингом ответа и
 * fallback-поведением, наружу открывая только прикладной порт.</p>
 */
@Component
class ExternalSystemCRestClient implements ExternalSystemCClient {

    private final RestClient externalSystemCRestClient;
    private final ExternalRestSystemsProperties properties;

    /**
     * Создает REST-адаптер.
     *
     * @param externalSystemCRestClient типизированный bean {@code RestClient} для system C
     * @param properties конфигурация REST-интеграции
     */
    ExternalSystemCRestClient(
            RestClient externalSystemCRestClient,
            ExternalRestSystemsProperties properties
    ) {
        this.externalSystemCRestClient = externalSystemCRestClient;
        this.properties = properties;
    }

    /**
     * Вызывает system C и маппит ответ в нормализованную модель внешнего сигнала.
     *
     * @param command команда агрегации с идентификаторами запроса и клиента
     * @return нормализованный сигнал от system C или fallback-сигнал о недоступности
     */
    @Override
    public ClientAggregationResult.ExternalSignal getClientSignal(ClientAggregationCommand command) {
        try {
            SystemCResponse response = externalSystemCRestClient.get()
                    .uri("/clients/{clientId}/signal?requestId={requestId}", command.clientId(), command.requestId())
                    .retrieve()
                    .body(SystemCResponse.class);

            if (response == null) {
                return ClientAggregationResult.ExternalSignal.unavailable("system-c", "EmptyResponse");
            }

            return new ClientAggregationResult.ExternalSignal("system-c", response.status(), response.value());
        } catch (RuntimeException ex) {
            if (properties.systemC().critical()) {
                throw ex;
            }
            return ClientAggregationResult.ExternalSignal.unavailable("system-c", ex.getClass().getSimpleName());
        }
    }

    /**
     * DTO ответа, принадлежащий REST-адаптеру system C.
     *
     * @param status статус уровня интеграции
     * @param value значение сигнала, специфичное для интеграции
     */
    record SystemCResponse(String status, String value) {
    }
}
