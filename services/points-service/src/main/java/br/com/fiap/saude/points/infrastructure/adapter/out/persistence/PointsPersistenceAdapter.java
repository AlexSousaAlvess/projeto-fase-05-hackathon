package br.com.fiap.saude.points.infrastructure.adapter.out.persistence;

import br.com.fiap.saude.points.domain.model.PointTransaction;
import br.com.fiap.saude.points.domain.model.PointsAccount;
import br.com.fiap.saude.points.domain.port.out.PointTransactionRepositoryPort;
import br.com.fiap.saude.points.domain.port.out.PointsAccountRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PointsPersistenceAdapter implements PointsAccountRepositoryPort, PointTransactionRepositoryPort {

    private final PointsAccountJpaRepository accountRepo;
    private final PointTransactionJpaRepository transactionRepo;

    @Override
    public PointsAccount save(PointsAccount account) {
        PointsAccountEntity entity = new PointsAccountEntity();
        entity.setId(account.getId());
        entity.setCitizenId(account.getCitizenId());
        entity.setBalance(account.getBalance());
        entity.setTotalEarned(account.getTotalEarned());
        entity.setTotalRedeemed(account.getTotalRedeemed());
        entity.setUpdatedAt(account.getUpdatedAt());
        PointsAccountEntity saved = accountRepo.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<PointsAccount> findByCitizenId(UUID citizenId) {
        return accountRepo.findByCitizenId(citizenId).map(this::toDomain);
    }

    @Override
    public PointTransaction save(PointTransaction tx) {
        PointTransactionEntity entity = new PointTransactionEntity();
        entity.setId(tx.getId());
        entity.setAccountId(tx.getAccountId());
        entity.setType(tx.getType());
        entity.setAmount(tx.getAmount());
        entity.setReason(tx.getReason());
        entity.setReferenceId(tx.getReferenceId());
        entity.setIdempotencyKey(tx.getIdempotencyKey());
        entity.setCreatedAt(tx.getCreatedAt());
        transactionRepo.save(entity);
        return tx;
    }

    @Override
    public boolean existsByIdempotencyKey(String key) {
        return transactionRepo.existsByIdempotencyKey(key);
    }

    private PointsAccount toDomain(PointsAccountEntity e) {
        return PointsAccount.builder()
                .id(e.getId()).citizenId(e.getCitizenId())
                .balance(e.getBalance()).totalEarned(e.getTotalEarned())
                .totalRedeemed(e.getTotalRedeemed()).updatedAt(e.getUpdatedAt())
                .build();
    }
}
