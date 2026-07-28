package br.com.fiap.saude.healthaction.infrastructure.adapter.in.web;

import br.com.fiap.saude.healthaction.domain.model.HealthAction;
import br.com.fiap.saude.healthaction.domain.port.in.FindHealthActionUseCase;
import br.com.fiap.saude.healthaction.domain.port.in.RegisterHealthActionUseCase;
import br.com.fiap.saude.healthaction.infrastructure.adapter.in.web.dto.HealthActionResponse;
import br.com.fiap.saude.healthaction.infrastructure.adapter.in.web.dto.RegisterHealthActionRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/health-actions")
@RequiredArgsConstructor
@Tag(name = "Ações de Saúde", description = "Registro de ações de saúde preventiva")
public class HealthActionController {

    private final RegisterHealthActionUseCase registerUseCase;
    private final FindHealthActionUseCase findUseCase;

    @PostMapping
    @Operation(summary = "Registrar ação de saúde", description = "Registra uma ação preventiva e credita pontos ao cidadão via evento Kafka")
    public ResponseEntity<HealthActionResponse> register(@Valid @RequestBody RegisterHealthActionRequest request) {
        HealthAction action = registerUseCase.register(new RegisterHealthActionUseCase.Command(
                request.citizenId(), request.actionType(), request.description(), request.proofDocumentUrl()
        ));
        return ResponseEntity.status(HttpStatus.CREATED).body(HealthActionResponse.from(action));
    }

    @GetMapping("/citizen/{citizenId}")
    @Operation(summary = "Listar ações por cidadão")
    public ResponseEntity<List<HealthActionResponse>> findByCitizen(@PathVariable UUID citizenId) {
        List<HealthActionResponse> actions = findUseCase.findByCitizenId(citizenId)
                .stream().map(HealthActionResponse::from).toList();
        return ResponseEntity.ok(actions);
    }
}
