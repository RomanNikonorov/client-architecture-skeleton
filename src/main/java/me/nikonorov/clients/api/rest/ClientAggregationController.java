package me.nikonorov.clients.api.rest;

import me.nikonorov.clients.application.usecase.ClientAggregationCommand;
import me.nikonorov.clients.application.usecase.ClientAggregationResult;
import me.nikonorov.clients.application.usecase.ClientAggregationUseCase;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Входной REST-адаптер для запросов агрегации клиента.
 *
 * <p>Контроллер намеренно остается тонким: валидирует тело HTTP-запроса, маппит
 * его в прикладную команду, делегирует сценарию использования и возвращает
 * транспортно-независимый прикладной результат.</p>
 */
@RestController
@RequestMapping("/api/v1/clients")
class ClientAggregationController {

    private final ClientAggregationUseCase useCase;

    /**
     * Создает контроллер.
     *
     * @param useCase прикладной сценарий, владеющий оркестрацией агрегации
     */
    ClientAggregationController(ClientAggregationUseCase useCase) {
        this.useCase = useCase;
    }

    /**
     * Обрабатывает {@code POST /api/v1/clients/aggregate}.
     *
     * @param request провалидированное тело REST-запроса
     * @return HTTP 200 ответ с результатом агрегации
     */
    @PostMapping("/aggregate")
    ResponseEntity<ClientAggregationResult> aggregate(@Valid @RequestBody ClientAggregationRequest request) {
        ClientAggregationCommand command = new ClientAggregationCommand(request.requestId(), request.clientId());
        return ResponseEntity.ok(useCase.aggregate(command));
    }

    /**
     * REST DTO запроса для endpoint агрегации.
     *
     * <p>Этот тип принадлежит только входному REST-адаптеру. Прикладной код
     * должен вместо него получать {@link ClientAggregationCommand}.</p>
     *
     * @param requestId корреляционный идентификатор запроса, переданный вызывающей стороной
     * @param clientId бизнес-идентификатор запрошенного клиента
     */
    record ClientAggregationRequest(
            @NotBlank String requestId,
            @NotBlank String clientId
    ) {
    }
}
