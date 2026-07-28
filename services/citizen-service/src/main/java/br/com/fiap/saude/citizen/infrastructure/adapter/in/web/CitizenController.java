package br.com.fiap.saude.citizen.infrastructure.adapter.in.web;

import br.com.fiap.saude.citizen.domain.port.in.FindCitizenUseCase;
import br.com.fiap.saude.citizen.domain.port.in.SyncCitizensFromSusUseCase;
import br.com.fiap.saude.citizen.infrastructure.adapter.in.web.dto.CitizenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/citizens")
@RequiredArgsConstructor
@Tag(name = "Cidadãos", description = "Sincronização e consulta de cidadãos vindos do SUS")
public class CitizenController {

    private final SyncCitizensFromSusUseCase syncCitizensFromSusUseCase;
    private final FindCitizenUseCase findCitizenUseCase;

    @PostMapping("/sync-sus")
    @Operation(summary = "Sincronizar cidadãos a partir da base do SUS")
    public ResponseEntity<SyncCitizensFromSusUseCase.SyncResult> syncSus() {
        return ResponseEntity.ok(syncCitizensFromSusUseCase.syncAll());
    }

    @GetMapping
    @Operation(summary = "Listar cidadãos sincronizados")
    public ResponseEntity<List<CitizenResponse>> findAll() {
        return ResponseEntity.ok(findCitizenUseCase.findAll().stream().map(CitizenResponse::from).toList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar cidadão por ID")
    public ResponseEntity<CitizenResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(CitizenResponse.from(findCitizenUseCase.findById(id)));
    }
}
