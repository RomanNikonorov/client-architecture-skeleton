package me.nikonorov.credit.application.port;

import me.nikonorov.credit.application.usecase.CreditDecisionCommand;
import me.nikonorov.credit.application.usecase.CreditDecisionResult;

/**
 * Прикладной порт внешней pricing-системы кредитного домена.
 */
public interface CreditPricingClient {

    /**
     * Получает тарифное предложение для кредитного продукта.
     *
     * @param command команда кредитного решения
     * @return нормализованное pricing-предложение
     */
    CreditDecisionResult.PricingOffer quote(CreditDecisionCommand command);
}
