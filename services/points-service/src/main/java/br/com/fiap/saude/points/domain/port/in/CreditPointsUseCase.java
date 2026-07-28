package br.com.fiap.saude.points.domain.port.in;

import java.util.UUID;

public interface CreditPointsUseCase {
    void credit(Command command);

    record Command(UUID citizenId, int amount, String reason, UUID referenceId, String idempotencyKey) {}
}
