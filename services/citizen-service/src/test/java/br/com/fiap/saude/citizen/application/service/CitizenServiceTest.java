package br.com.fiap.saude.citizen.application.service;

import br.com.fiap.saude.citizen.domain.model.Citizen;
import br.com.fiap.saude.citizen.domain.model.CitizenStatus;
import br.com.fiap.saude.citizen.domain.port.in.SyncCitizensFromSusUseCase;
import br.com.fiap.saude.citizen.domain.port.out.CitizenRepositoryPort;
import br.com.fiap.saude.citizen.domain.port.out.SusClientPort;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CitizenServiceTest {

    @Mock
    private CitizenRepositoryPort repository;

    @Mock
    private SusClientPort susClient;

    @InjectMocks
    private CitizenService service;

    private static final SusClientPort.SusCitizenData SUS_CITIZEN = new SusClientPort.SusCitizenData(
            "123.456.789-00", "João Silva", "joao@email.com", "11999999999", LocalDate.of(1990, 1, 15)
    );

    @Test
    void syncAll_savesNewCitizensFromSus() {
        when(susClient.fetchCitizens()).thenReturn(List.of(SUS_CITIZEN));
        when(repository.existsByCpf(SUS_CITIZEN.cpf())).thenReturn(false);

        SyncCitizensFromSusUseCase.SyncResult result = service.syncAll();

        assertThat(result.synced()).isEqualTo(1);
        assertThat(result.skipped()).isEqualTo(0);
        verify(repository).save(any(Citizen.class));
    }

    @Test
    void syncAll_skipsCitizensAlreadyPersisted() {
        when(susClient.fetchCitizens()).thenReturn(List.of(SUS_CITIZEN));
        when(repository.existsByCpf(SUS_CITIZEN.cpf())).thenReturn(true);

        SyncCitizensFromSusUseCase.SyncResult result = service.syncAll();

        assertThat(result.synced()).isEqualTo(0);
        assertThat(result.skipped()).isEqualTo(1);
        verify(repository, never()).save(any());
    }

    @Test
    void findById_returnsCitizenWhenFound() {
        UUID id = UUID.randomUUID();
        Citizen citizen = Citizen.builder()
                .id(id).cpf("123.456.789-00").name("João Silva")
                .email("joao@email.com").phone("11999999999")
                .birthDate(LocalDate.of(1990, 1, 15)).status(CitizenStatus.ACTIVE).build();
        when(repository.findById(id)).thenReturn(Optional.of(citizen));

        Citizen result = service.findById(id);

        assertThat(result.getId()).isEqualTo(id);
    }

    @Test
    void findById_throwsWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(id))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Cidadão não encontrado");
    }

    @Test
    void findAll_returnsAllCitizens() {
        Citizen citizen = Citizen.builder().id(UUID.randomUUID()).cpf("123.456.789-00").build();
        when(repository.findAll()).thenReturn(List.of(citizen));

        List<Citizen> result = service.findAll();

        assertThat(result).hasSize(1);
    }
}
