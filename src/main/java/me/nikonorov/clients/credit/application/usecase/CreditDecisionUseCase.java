package me.nikonorov.clients.credit.application.usecase;

import me.nikonorov.clients.application.fanout.AsyncProperties;
import me.nikonorov.clients.application.fanout.FanOutExecutor;
import me.nikonorov.clients.credit.application.port.CreditPricingClient;
import me.nikonorov.clients.credit.application.port.CreditScoringClient;
import me.nikonorov.clients.credit.domain.CreditPolicy;
import me.nikonorov.clients.credit.domain.CreditPolicyRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Оркестрирует сценарий кредитного решения.
 *
 * <p>Use case принадлежит bounded context {@code credit}: он зависит только от
 * доменных контрактов своего домена, application-портов своего домена и shared
 * fan-out API. Это позволяет в будущем вынести домен в отдельный сервис без
 * протаскивания типов клиентской агрегации.</p>
 */
@Service
public class CreditDecisionUseCase {

    private static final int MIN_APPROVAL_SCORE = 650;

    private final CreditPolicyRepository policies;
    private final CreditScoringClient scoring;
    private final CreditPricingClient pricing;
    private final FanOutExecutor fanOutExecutor;
    private final int maxParallelTasksPerRequest;

    /**
     * Создает сценарий кредитного решения.
     *
     * @param policies доменный репозиторий кредитных политик
     * @param scoring исходящий порт scoring
     * @param pricing исходящий порт pricing
     * @param fanOutExecutor центральный ограниченный fan-out executor
     * @param asyncProperties конфигурация fan-out лимита
     */
    public CreditDecisionUseCase(
            CreditPolicyRepository policies,
            CreditScoringClient scoring,
            CreditPricingClient pricing,
            FanOutExecutor fanOutExecutor,
            AsyncProperties asyncProperties
    ) {
        this.policies = policies;
        this.scoring = scoring;
        this.pricing = pricing;
        this.fanOutExecutor = fanOutExecutor;
        this.maxParallelTasksPerRequest = asyncProperties.maxParallelTasksPerRequest();
    }

    /**
     * Оценивает кредитное решение для одного клиента.
     *
     * @param command провалидированная прикладная команда
     * @return итоговое кредитное решение
     */
    public CreditDecisionResult evaluate(CreditDecisionCommand command) {
        FanOutExecutor.FanOutScope fanOut = fanOutExecutor.openScope(maxParallelTasksPerRequest);

        FanOutExecutor.FanOutTask<CreditPolicy> policyTask = fanOut.submit(
                () -> policies.findByClientId(command.clientId()));
        FanOutExecutor.FanOutTask<CreditDecisionResult.ScoringAssessment> scoringTask = fanOut.submit(
                () -> scoring.assess(command));
        FanOutExecutor.FanOutTask<CreditDecisionResult.PricingOffer> pricingTask = fanOut.submit(
                () -> pricing.quote(command));

        CreditPolicy policy = policyTask.join();
        CreditDecisionResult.ScoringAssessment scoringAssessment = scoringTask.join();
        CreditDecisionResult.PricingOffer pricingOffer = pricingTask.join();

        List<String> warnings = new ArrayList<>();
        if ("UNAVAILABLE".equals(scoringAssessment.status())) {
            warnings.add("credit-scoring unavailable");
        }
        if ("UNAVAILABLE".equals(pricingOffer.status())) {
            warnings.add("credit-pricing unavailable");
        }

        boolean approved = !policy.blocked()
                && "OK".equals(scoringAssessment.status())
                && "OK".equals(pricingOffer.status())
                && scoringAssessment.score() >= MIN_APPROVAL_SCORE
                && command.requestedAmount() <= policy.maxLimit()
                && command.requestedAmount() <= scoringAssessment.recommendedLimit();
        int approvedLimit = approved
                ? Math.min(policy.maxLimit(), scoringAssessment.recommendedLimit())
                : 0;

        return new CreditDecisionResult(
                command.requestId(),
                command.clientId(),
                approved,
                approvedLimit,
                pricingOffer.ratePlan(),
                pricingOffer.annualRateBasisPoints(),
                scoringAssessment,
                pricingOffer,
                warnings
        );
    }
}
