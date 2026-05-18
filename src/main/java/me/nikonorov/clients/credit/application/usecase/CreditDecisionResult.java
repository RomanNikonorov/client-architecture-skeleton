package me.nikonorov.clients.credit.application.usecase;

import java.util.List;

/**
 * Прикладной результат кредитного решения.
 *
 * @param requestId корреляционный идентификатор запроса
 * @param clientId бизнес-идентификатор клиента
 * @param approved итоговое решение
 * @param approvedLimit одобренный лимит или {@code 0}, если решение отрицательное
 * @param ratePlan тарифный план
 * @param annualRateBasisPoints годовая ставка в basis points
 * @param scoring нормализованный результат scoring
 * @param pricing нормализованный результат pricing
 * @param warnings предупреждения по некритичным fallback-результатам
 */
public record CreditDecisionResult(
        String requestId,
        String clientId,
        boolean approved,
        int approvedLimit,
        String ratePlan,
        int annualRateBasisPoints,
        ScoringAssessment scoring,
        PricingOffer pricing,
        List<String> warnings
) {

    /**
     * Нормализованная оценка внешней scoring-системы.
     *
     * @param source имя источника
     * @param status статус интеграции
     * @param score скоринговый балл
     * @param recommendedLimit рекомендованный лимит
     */
    public record ScoringAssessment(String source, String status, int score, int recommendedLimit) {

        /**
         * Создает fallback-результат недоступности scoring.
         *
         * @param source имя источника
         * @param reason причина недоступности
         * @return нормализованный unavailable результат
         */
        public static ScoringAssessment unavailable(String source, String reason) {
            return new ScoringAssessment(source, "UNAVAILABLE", 0, 0);
        }
    }

    /**
     * Нормализованное ценовое предложение внешней pricing-системы.
     *
     * @param source имя источника
     * @param status статус интеграции
     * @param ratePlan выбранный тарифный план
     * @param annualRateBasisPoints годовая ставка в basis points
     */
    public record PricingOffer(String source, String status, String ratePlan, int annualRateBasisPoints) {

        /**
         * Создает fallback-результат недоступности pricing.
         *
         * @param source имя источника
         * @param reason причина недоступности
         * @return нормализованный unavailable результат
         */
        public static PricingOffer unavailable(String source, String reason) {
            return new PricingOffer(source, "UNAVAILABLE", reason, 0);
        }
    }
}
