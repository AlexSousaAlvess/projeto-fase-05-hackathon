package br.com.fiap.saude.points.application.service;

import br.com.fiap.saude.points.domain.model.PointTransaction;
import br.com.fiap.saude.points.domain.model.PointsAccount;
import br.com.fiap.saude.points.domain.model.TransactionType;
import br.com.fiap.saude.points.domain.port.in.CreditPointsUseCase;
import br.com.fiap.saude.points.domain.port.in.DebitPointsUseCase;
import br.com.fiap.saude.points.domain.port.in.GetBalanceUseCase;
import br.com.fiap.saude.points.domain.port.out.PointTransactionRepositoryPort;
import br.com.fiap.saude.points.domain.port.out.PointsAccountRepositoryPort;
import br.com.fiap.saude.points.domain.port.out.PointsEventPublisherPort;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PointsService implements CreditPointsUseCase, DebitPointsUseCase, GetBalanceUseCase {

    private final PointsAccountRepositoryPort accountRepository;
    private final PointTransactionRepositoryPort transactionRepository;
    private final PointsEventPublisherPort eventPublisher;

    @Override
    @Transactional
    public void credit(CreditPointsUseCase.Command command) {
        if (transactionRepository.existsByIdempotencyKey(command.idempotencyKey())) {
            log.warn("Evento já processado (idempotência): {}", command.idempotencyKey());
            return;
        }

        PointsAccount account = accountRepository.findByCitizenId(command.citizenId())
                .orElseGet(() -> createAccount(command.citizenId()));

        PointsAccount updated = PointsAccount.builder()
                .id(account.getId())
                .citizenId(account.getCitizenId())
                .balance(account.getBalance() + command.amount())
                .totalEarned(account.getTotalEarned() + command.amount())
                .totalRedeemed(account.getTotalRedeemed())
                .updatedAt(LocalDateTime.now())
                .build();

        accountRepository.save(updated);

        transactionRepository.save(PointTransaction.builder()
                .id(UUID.randomUUID())
                .accountId(updated.getId())
                .type(TransactionType.CREDIT)
                .amount(command.amount())
                .reason(command.reason())
                .referenceId(command.referenceId())
                .idempotencyKey(command.idempotencyKey())
                .createdAt(LocalDateTime.now())
                .build());

        eventPublisher.publishPointsCredited(updated, command.amount(), command.reason());
        log.info("Pontos creditados: {} para cidadão {}. Saldo: {}", command.amount(), command.citizenId(), updated.getBalance());
    }

    @Override
    @Transactional
    public void debit(DebitPointsUseCase.Command command) {
        if (transactionRepository.existsByIdempotencyKey(command.idempotencyKey())) {
            log.warn("Evento já processado (idempotência): {}", command.idempotencyKey());
            return;
        }

        PointsAccount account = accountRepository.findByCitizenId(command.citizenId())
                .orElseThrow(() -> new EntityNotFoundException("Conta não encontrada para cidadão: " + command.citizenId()));

        if (!account.hasSufficientBalance(command.amount())) {
            log.warn("Saldo insuficiente para cidadão {}. Saldo: {}, Necessário: {}", command.citizenId(), account.getBalance(), command.amount());
            eventPublisher.publishPointsDebitFailed(command.citizenId(), command.redemptionId(), account.getBalance(), command.amount());
            return;
        }

        PointsAccount updated = PointsAccount.builder()
                .id(account.getId())
                .citizenId(account.getCitizenId())
                .balance(account.getBalance() - command.amount())
                .totalEarned(account.getTotalEarned())
                .totalRedeemed(account.getTotalRedeemed() + command.amount())
                .updatedAt(LocalDateTime.now())
                .build();

        accountRepository.save(updated);

        transactionRepository.save(PointTransaction.builder()
                .id(UUID.randomUUID())
                .accountId(updated.getId())
                .type(TransactionType.DEBIT)
                .amount(command.amount())
                .reason("Resgate de recompensa")
                .referenceId(command.redemptionId())
                .idempotencyKey(command.idempotencyKey())
                .createdAt(LocalDateTime.now())
                .build());

        eventPublisher.publishPointsDebited(updated, command.amount(), command.redemptionId());
        log.info("Pontos debitados: {} do cidadão {}. Saldo: {}", command.amount(), command.citizenId(), updated.getBalance());
    }

    @Override
    public PointsAccount getBalance(UUID citizenId) {
        return accountRepository.findByCitizenId(citizenId)
                .orElseThrow(() -> new EntityNotFoundException("Conta não encontrada para cidadão: " + citizenId));
    }

    private PointsAccount createAccount(UUID citizenId) {
        PointsAccount account = PointsAccount.builder()
                .id(UUID.randomUUID())
                .citizenId(citizenId)
                .balance(0)
                .totalEarned(0)
                .totalRedeemed(0)
                .updatedAt(LocalDateTime.now())
                .build();
        return accountRepository.save(account);
    }
}
