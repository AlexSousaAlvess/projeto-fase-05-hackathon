package br.com.fiap.saude.points.infrastructure.adapter.in.web;

import br.com.fiap.saude.points.domain.model.PointsAccount;
import br.com.fiap.saude.points.domain.port.in.GetBalanceUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/points")
@RequiredArgsConstructor
@Tag(name = "Pontos", description = "Consulta de saldo e transações de pontos")
public class PointsController {

    private final GetBalanceUseCase getBalanceUseCase;

    @GetMapping("/{citizenId}")
    @Operation(summary = "Consultar saldo de pontos do cidadão")
    public ResponseEntity<BalanceResponse> getBalance(@PathVariable UUID citizenId) {
        PointsAccount account = getBalanceUseCase.getBalance(citizenId);
        return ResponseEntity.ok(new BalanceResponse(
                account.getCitizenId(), account.getBalance(),
                account.getTotalEarned(), account.getTotalRedeemed()
        ));
    }

    public record BalanceResponse(UUID citizenId, int balance, int totalEarned, int totalRedeemed) {}
}
