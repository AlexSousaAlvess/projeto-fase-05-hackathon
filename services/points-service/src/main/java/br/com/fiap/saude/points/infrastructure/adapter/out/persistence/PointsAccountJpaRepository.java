package br.com.fiap.saude.points.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PointsAccountJpaRepository extends JpaRepository<PointsAccountEntity, UUID> {
    Optional<PointsAccountEntity> findByCitizenId(UUID citizenId);
}
