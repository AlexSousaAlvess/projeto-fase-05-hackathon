package br.com.fiap.saude.points.domain.port.out;

import br.com.fiap.saude.points.domain.model.PointTransaction;

public interface PointTransactionRepositoryPort {
    PointTransaction save(PointTransaction transaction);
    boolean existsByIdempotencyKey(String key);
}
