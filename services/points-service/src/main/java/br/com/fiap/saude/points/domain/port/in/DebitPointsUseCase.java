package br.com.fiap.saude.points.domain.port.in;

import java.util.UUID;

public interface DebitPointsUseCase {
    void debit(Command command);

    record Command(UUID citizenId, int amount, UUID redemptionId, String idempotencyKey) {}
}
