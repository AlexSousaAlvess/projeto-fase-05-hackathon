package br.com.fiap.saude.points.domain.port.out;

import br.com.fiap.saude.points.domain.model.PointsAccount;

import java.util.UUID;

public interface PointsEventPublisherPort {
    void publishPointsCredited(PointsAccount account, int amount, String reason);
    void publishPointsDebited(PointsAccount account, int amount, UUID redemptionId);
    void publishPointsDebitFailed(UUID citizenId, UUID redemptionId, int currentBalance, int required);
}
