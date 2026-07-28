package br.com.fiap.saude.points.domain.port.in;

import br.com.fiap.saude.points.domain.model.PointsAccount;

import java.util.UUID;

public interface GetBalanceUseCase {
    PointsAccount getBalance(UUID citizenId);
}
