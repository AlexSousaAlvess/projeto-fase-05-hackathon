package br.com.fiap.saude.points.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PointTransactionJpaRepository extends JpaRepository<PointTransactionEntity, UUID> {
    boolean existsByIdempotencyKey(String idempotencyKey);
}
