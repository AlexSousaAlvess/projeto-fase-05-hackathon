package br.com.fiap.saude.points.application.service;

import br.com.fiap.saude.points.domain.model.PointsAccount;
import br.com.fiap.saude.points.domain.port.in.CreditPointsUseCase;
import br.com.fiap.saude.points.domain.port.in.DebitPointsUseCase;
import br.com.fiap.saude.points.domain.port.out.PointTransactionRepositoryPort;
import br.com.fiap.saude.points.domain.port.out.PointsAccountRepositoryPort;
import br.com.fiap.saude.points.domain.port.out.PointsEventPublisherPort;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PointsServiceTest {

    @Mock
    private PointsAccountRepositoryPort accountRepository;

    @Mock
    private PointTransactionRepositoryPort transactionRepository;

    @Mock
    private PointsEventPublisherPort eventPublisher;

    @InjectMocks
    private PointsService service;

    // --- credit ---

    @Test
    void credit_createsNewAccountWhenNoneExists() {
        UUID citizenId = UUID.randomUUID();
        CreditPointsUseCase.Command command = new CreditPointsUseCase.Command(
                citizenId, 100, "Vacinação", UUID.randomUUID(), "key-1"
        );
        when(transactionRepository.existsByIdempotencyKey("key-1")).thenReturn(false);
        when(accountRepository.findByCitizenId(citizenId)).thenReturn(Optional.empty());

        PointsAccount newAccount = buildAccount(citizenId, 0, 0, 0);
        when(accountRepository.save(any())).thenReturn(newAccount);

        service.credit(command);

        ArgumentCaptor<PointsAccount> captor = ArgumentCaptor.forClass(PointsAccount.class);
        verify(accountRepository, times(2)).save(captor.capture());
        verify(transactionRepository).save(any());
        verify(eventPublisher).publishPointsCredited(any(), eq(100), eq("Vacinação"));
    }

    @Test
    void credit_updatesExistingAccountBalance() {
        UUID citizenId = UUID.randomUUID();
        CreditPointsUseCase.Command command = new CreditPointsUseCase.Command(
                citizenId, 150, "Exame preventivo", UUID.randomUUID(), "key-2"
        );
        PointsAccount existing = buildAccount(citizenId, 200, 200, 0);
        when(transactionRepository.existsByIdempotencyKey("key-2")).thenReturn(false);
        when(accountRepository.findByCitizenId(citizenId)).thenReturn(Optional.of(existing));
        when(accountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.credit(command);

        ArgumentCaptor<PointsAccount> captor = ArgumentCaptor.forClass(PointsAccount.class);
        verify(accountRepository).save(captor.capture());
        assertThat(captor.getValue().getBalance()).isEqualTo(350);
        assertThat(captor.getValue().getTotalEarned()).isEqualTo(350);
    }

    @Test
    void credit_isIdempotent() {
        UUID citizenId = UUID.randomUUID();
        CreditPointsUseCase.Command command = new CreditPointsUseCase.Command(
                citizenId, 100, "reason", UUID.randomUUID(), "duplicate-key"
        );
        when(transactionRepository.existsByIdempotencyKey("duplicate-key")).thenReturn(true);

        service.credit(command);

        verify(accountRepository, never()).save(any());
        verify(eventPublisher, never()).publishPointsCredited(any(), anyInt(), any());
    }

    // --- debit ---

    @Test
    void debit_reducesBalanceWhenSufficient() {
        UUID citizenId = UUID.randomUUID();
        UUID redemptionId = UUID.randomUUID();
        DebitPointsUseCase.Command command = new DebitPointsUseCase.Command(
                citizenId, 100, redemptionId, "debit-key-1"
        );
        PointsAccount account = buildAccount(citizenId, 300, 300, 0);
        when(transactionRepository.existsByIdempotencyKey("debit-key-1")).thenReturn(false);
        when(accountRepository.findByCitizenId(citizenId)).thenReturn(Optional.of(account));
        when(accountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.debit(command);

        ArgumentCaptor<PointsAccount> captor = ArgumentCaptor.forClass(PointsAccount.class);
        verify(accountRepository).save(captor.capture());
        assertThat(captor.getValue().getBalance()).isEqualTo(200);
        assertThat(captor.getValue().getTotalRedeemed()).isEqualTo(100);
        verify(eventPublisher).publishPointsDebited(any(), eq(100), eq(redemptionId));
    }

    @Test
    void debit_publishesFailureEventWhenBalanceInsufficient() {
        UUID citizenId = UUID.randomUUID();
        UUID redemptionId = UUID.randomUUID();
        DebitPointsUseCase.Command command = new DebitPointsUseCase.Command(
                citizenId, 500, redemptionId, "debit-key-2"
        );
        PointsAccount account = buildAccount(citizenId, 100, 100, 0);
        when(transactionRepository.existsByIdempotencyKey("debit-key-2")).thenReturn(false);
        when(accountRepository.findByCitizenId(citizenId)).thenReturn(Optional.of(account));

        service.debit(command);

        verify(accountRepository, never()).save(any());
        verify(eventPublisher).publishPointsDebitFailed(citizenId, redemptionId, 100, 500);
    }

    @Test
    void debit_throwsWhenAccountNotFound() {
        UUID citizenId = UUID.randomUUID();
        DebitPointsUseCase.Command command = new DebitPointsUseCase.Command(
                citizenId, 100, UUID.randomUUID(), "debit-key-3"
        );
        when(transactionRepository.existsByIdempotencyKey("debit-key-3")).thenReturn(false);
        when(accountRepository.findByCitizenId(citizenId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.debit(command))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Conta não encontrada");
    }

    @Test
    void debit_isIdempotent() {
        UUID citizenId = UUID.randomUUID();
        DebitPointsUseCase.Command command = new DebitPointsUseCase.Command(
                citizenId, 100, UUID.randomUUID(), "duplicate-debit-key"
        );
        when(transactionRepository.existsByIdempotencyKey("duplicate-debit-key")).thenReturn(true);

        service.debit(command);

        verify(accountRepository, never()).findByCitizenId(any());
    }

    // --- getBalance ---

    @Test
    void getBalance_returnsAccountWhenFound() {
        UUID citizenId = UUID.randomUUID();
        PointsAccount account = buildAccount(citizenId, 250, 250, 0);
        when(accountRepository.findByCitizenId(citizenId)).thenReturn(Optional.of(account));

        PointsAccount result = service.getBalance(citizenId);

        assertThat(result.getBalance()).isEqualTo(250);
    }

    @Test
    void getBalance_throwsWhenAccountNotFound() {
        UUID citizenId = UUID.randomUUID();
        when(accountRepository.findByCitizenId(citizenId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getBalance(citizenId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Conta não encontrada");
    }

    private PointsAccount buildAccount(UUID citizenId, int balance, int totalEarned, int totalRedeemed) {
        return PointsAccount.builder()
                .id(UUID.randomUUID())
                .citizenId(citizenId)
                .balance(balance)
                .totalEarned(totalEarned)
                .totalRedeemed(totalRedeemed)
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
