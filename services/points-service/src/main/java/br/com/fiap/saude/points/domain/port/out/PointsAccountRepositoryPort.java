package br.com.fiap.saude.points.domain.port.out;

import br.com.fiap.saude.points.domain.model.PointsAccount;

import java.util.Optional;
import java.util.UUID;

public interface PointsAccountRepositoryPort {
    PointsAccount save(PointsAccount account);
    Optional<PointsAccount> findByCitizenId(UUID citizenId);
}
