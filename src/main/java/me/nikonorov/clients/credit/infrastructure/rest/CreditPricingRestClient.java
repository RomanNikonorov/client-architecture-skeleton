package me.nikonorov.clients.credit.infrastructure.rest;

import me.nikonorov.clients.credit.application.port.CreditPricingClient;
import me.nikonorov.clients.credit.application.usecase.CreditDecisionCommand;
import me.nikonorov.clients.credit.application.usecase.CreditDecisionResult;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Исходящий REST-адаптер внешней pricing-системы кредитного домена.
 */
@Component
class CreditPricingRestClient implements CreditPricingClient {

    private final RestClient creditPricingRestClient;
    private final CreditRestSystemsProperties properties;

    /**
     * Создает REST-адаптер.
     *
     * @param creditPricingRestClient типизированный bean {@code RestClient} для pricing
     * @param properties конфигурация REST-интеграции
     */
    CreditPricingRestClient(
            RestClient creditPricingRestClient,
            CreditRestSystemsProperties properties
    ) {
        this.creditPricingRestClient = creditPricingRestClient;
        this.properties = properties;
    }

    /**
     * Вызывает pricing и маппит ответ в прикладную модель кредитного домена.
     *
     * @param command команда кредитного решения
     * @return нормализованное pricing-предложение
     */
    @Override
    public CreditDecisionResult.PricingOffer quote(CreditDecisionCommand command) {
        try {
            PricingResponse response = creditPricingRestClient.get()
                    .uri("/credit/pricing/{clientId}?requestId={requestId}&amount={amount}",
                            command.clientId(),
                            command.requestId(),
                            command.requestedAmount())
                    .retrieve()
                    .body(PricingResponse.class);

            if (response == null) {
                return CreditDecisionResult.PricingOffer.unavailable("credit-pricing", "EmptyResponse");
            }

            return new CreditDecisionResult.PricingOffer(
                    "credit-pricing",
                    response.status(),
                    response.ratePlan(),
                    response.annualRateBasisPoints()
            );
        } catch (RuntimeException ex) {
            if (properties.pricing().critical()) {
                throw ex;
            }
            return CreditDecisionResult.PricingOffer.unavailable(
                    "credit-pricing",
                    ex.getClass().getSimpleName());
        }
    }

    /**
     * DTO ответа, принадлежащий REST-адаптеру pricing.
     *
     * @param status статус уровня интеграции
     * @param ratePlan тарифный план
     * @param annualRateBasisPoints годовая ставка в basis points
     */
    record PricingResponse(String status, String ratePlan, int annualRateBasisPoints) {
    }
}
