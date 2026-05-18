package me.nikonorov.clients.credit.infrastructure.grpc;

import me.nikonorov.clients.credit.application.port.CreditScoringClient;
import me.nikonorov.clients.credit.application.usecase.CreditDecisionCommand;
import me.nikonorov.clients.credit.application.usecase.CreditDecisionResult;
import me.nikonorov.clients.credit.infrastructure.grpc.generated.CreditScoringGrpc;
import me.nikonorov.clients.credit.infrastructure.grpc.generated.CreditScoringRequest;
import me.nikonorov.clients.credit.infrastructure.grpc.generated.CreditScoringResponse;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Исходящий gRPC-адаптер внешней scoring-системы кредитного домена.
 */
@Component
class CreditScoringGrpcClient implements CreditScoringClient {

    private final CreditScoringGrpc.CreditScoringBlockingStub stub;
    private final CreditExternalSystemsProperties properties;

    /**
     * Создает адаптер.
     *
     * @param stub сгенерированный blocking stub scoring-системы
     * @param properties deadline- и fallback-конфигурация scoring
     */
    CreditScoringGrpcClient(
            CreditScoringGrpc.CreditScoringBlockingStub stub,
            CreditExternalSystemsProperties properties
    ) {
        this.stub = stub;
        this.properties = properties;
    }

    /**
     * Вызывает scoring и маппит ответ в прикладную модель кредитного домена.
     *
     * @param command команда кредитного решения
     * @return нормализованная scoring-оценка
     */
    @Override
    public CreditDecisionResult.ScoringAssessment assess(CreditDecisionCommand command) {
        try {
            CreditScoringRequest request = CreditScoringRequest.newBuilder()
                    .setRequestId(command.requestId())
                    .setClientId(command.clientId())
                    .setRequestedAmount(command.requestedAmount())
                    .build();

            CreditScoringResponse response = stub.withDeadlineAfter(
                            properties.scoring().deadline().toMillis(),
                            TimeUnit.MILLISECONDS)
                    .assess(request);

            return new CreditDecisionResult.ScoringAssessment(
                    "credit-scoring",
                    response.getStatus(),
                    response.getScore(),
                    response.getRecommendedLimit()
            );
        } catch (RuntimeException ex) {
            if (properties.scoring().critical()) {
                throw ex;
            }
            return CreditDecisionResult.ScoringAssessment.unavailable(
                    "credit-scoring",
                    ex.getClass().getSimpleName());
        }
    }
}
