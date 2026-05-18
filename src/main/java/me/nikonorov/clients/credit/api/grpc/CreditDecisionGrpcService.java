package me.nikonorov.clients.credit.api.grpc;

import io.grpc.stub.StreamObserver;
import me.nikonorov.clients.credit.api.grpc.generated.CreditDecisionApiGrpc;
import me.nikonorov.clients.credit.api.grpc.generated.CreditDecisionRequest;
import me.nikonorov.clients.credit.api.grpc.generated.CreditDecisionResponse;
import me.nikonorov.clients.credit.api.grpc.generated.PricingOfferMessage;
import me.nikonorov.clients.credit.api.grpc.generated.ScoringAssessmentMessage;
import me.nikonorov.clients.credit.application.usecase.CreditDecisionCommand;
import me.nikonorov.clients.credit.application.usecase.CreditDecisionResult;
import me.nikonorov.clients.credit.application.usecase.CreditDecisionUseCase;
import org.springframework.grpc.server.service.GrpcService;

/**
 * Входной gRPC-адаптер кредитного bounded context.
 */
@GrpcService
class CreditDecisionGrpcService extends CreditDecisionApiGrpc.CreditDecisionApiImplBase {

    private final CreditDecisionUseCase useCase;

    /**
     * Создает gRPC service.
     *
     * @param useCase прикладной сценарий кредитного решения
     */
    CreditDecisionGrpcService(CreditDecisionUseCase useCase) {
        this.useCase = useCase;
    }

    /**
     * Обрабатывает unary gRPC method {@code DecideCredit}.
     *
     * @param request сгенерированное gRPC-сообщение запроса
     * @param responseObserver gRPC response observer для отправки результата
     */
    @Override
    public void decideCredit(
            CreditDecisionRequest request,
            StreamObserver<CreditDecisionResponse> responseObserver
    ) {
        CreditDecisionResult result = useCase.evaluate(new CreditDecisionCommand(
                request.getRequestId(),
                request.getClientId(),
                request.getRequestedAmount()));

        responseObserver.onNext(toResponse(result));
        responseObserver.onCompleted();
    }

    /**
     * Маппит прикладной результат в gRPC response.
     *
     * @param result прикладной результат кредитного решения
     * @return сгенерированный gRPC response
     */
    private CreditDecisionResponse toResponse(CreditDecisionResult result) {
        return CreditDecisionResponse.newBuilder()
                .setRequestId(result.requestId())
                .setClientId(result.clientId())
                .setApproved(result.approved())
                .setApprovedLimit(result.approvedLimit())
                .setRatePlan(result.ratePlan())
                .setAnnualRateBasisPoints(result.annualRateBasisPoints())
                .setScoring(toScoring(result.scoring()))
                .setPricing(toPricing(result.pricing()))
                .addAllWarnings(result.warnings())
                .build();
    }

    private ScoringAssessmentMessage toScoring(CreditDecisionResult.ScoringAssessment scoring) {
        return ScoringAssessmentMessage.newBuilder()
                .setSource(scoring.source())
                .setStatus(scoring.status())
                .setScore(scoring.score())
                .setRecommendedLimit(scoring.recommendedLimit())
                .build();
    }

    private PricingOfferMessage toPricing(CreditDecisionResult.PricingOffer pricing) {
        return PricingOfferMessage.newBuilder()
                .setSource(pricing.source())
                .setStatus(pricing.status())
                .setRatePlan(pricing.ratePlan())
                .setAnnualRateBasisPoints(pricing.annualRateBasisPoints())
                .build();
    }
}
