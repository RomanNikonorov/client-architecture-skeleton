package me.nikonorov.clients.api.rest;

import me.nikonorov.clients.application.ClientAggregationCommand;
import me.nikonorov.clients.application.ClientAggregationResult;
import me.nikonorov.clients.application.ClientAggregationUseCase;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST inbound adapter for client aggregation requests.
 *
 * <p>The controller stays intentionally thin: it validates the HTTP request
 * body, maps it to an application command, delegates to the use case, and
 * returns the transport-neutral application result.</p>
 */
@RestController
@RequestMapping("/api/v1/clients")
class ClientAggregationController {

    private final ClientAggregationUseCase useCase;

    /**
     * Creates the controller.
     *
     * @param useCase application use case that owns aggregation orchestration
     */
    ClientAggregationController(ClientAggregationUseCase useCase) {
        this.useCase = useCase;
    }

    /**
     * Handles {@code POST /api/v1/clients/aggregate}.
     *
     * @param request validated REST request body
     * @return HTTP 200 response with the aggregation result
     */
    @PostMapping("/aggregate")
    ResponseEntity<ClientAggregationResult> aggregate(@Valid @RequestBody ClientAggregationRequest request) {
        ClientAggregationCommand command = new ClientAggregationCommand(request.requestId(), request.clientId());
        return ResponseEntity.ok(useCase.aggregate(command));
    }

    /**
     * REST request DTO for the aggregation endpoint.
     *
     * <p>This type is private to the inbound REST adapter. Application code must
     * receive {@link ClientAggregationCommand} instead.</p>
     *
     * @param requestId caller-provided request correlation identifier
     * @param clientId business identifier of the requested client
     */
    record ClientAggregationRequest(
            @NotBlank String requestId,
            @NotBlank String clientId
    ) {
    }
}
