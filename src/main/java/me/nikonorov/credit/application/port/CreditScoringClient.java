package me.nikonorov.credit.application.port;

import me.nikonorov.credit.application.usecase.CreditDecisionCommand;
import me.nikonorov.credit.application.usecase.CreditDecisionResult;

/**
 * Прикладной порт внешней scoring-системы кредитного домена.
 */
public interface CreditScoringClient {

    /**
     * Получает внешнюю скоринговую оценку клиента.
     *
     * @param command команда кредитного решения
     * @return нормализованная scoring-оценка
     */
    CreditDecisionResult.ScoringAssessment assess(CreditDecisionCommand command);
}
