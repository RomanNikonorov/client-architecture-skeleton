package me.nikonorov.credit.api.rest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import me.nikonorov.credit.application.usecase.CreditDecisionCommand;
import me.nikonorov.credit.application.usecase.CreditDecisionResult;
import me.nikonorov.credit.application.usecase.CreditDecisionUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Входной REST-адаптер кредитного bounded context.
 *
 * <p>Контроллер остается тонким: валидирует REST DTO, маппит его в прикладную
 * команду домена {@code credit} и делегирует бизнес-оркестрацию use case.</p>
 */
@RestController
@RequestMapping("/api/v1/credit")
class CreditDecisionController {

    private final CreditDecisionUseCase useCase;

    /**
     * Создает контроллер.
     *
     * @param useCase прикладной сценарий кредитного решения
     */
    CreditDecisionController(CreditDecisionUseCase useCase) {
        this.useCase = useCase;
    }

    /**
     * Обрабатывает {@code POST /api/v1/credit/decisions}.
     *
     * @param request провалидированное тело REST-запроса
     * @return HTTP 200 ответ с результатом кредитного решения
     */
    @PostMapping("/decisions")
    ResponseEntity<CreditDecisionResult> decide(@Valid @RequestBody CreditDecisionRequest request) {
        CreditDecisionCommand command = new CreditDecisionCommand(
                request.requestId(),
                request.clientId(),
                request.requestedAmount());
        return ResponseEntity.ok(useCase.evaluate(command));
    }

    /**
     * REST DTO запроса кредитного решения.
     *
     * @param requestId корреляционный идентификатор запроса
     * @param clientId бизнес-идентификатор клиента
     * @param requestedAmount запрошенная сумма кредитного продукта
     */
    record CreditDecisionRequest(
            @NotBlank String requestId,
            @NotBlank String clientId,
            @Min(1) int requestedAmount
    ) {
    }
}
