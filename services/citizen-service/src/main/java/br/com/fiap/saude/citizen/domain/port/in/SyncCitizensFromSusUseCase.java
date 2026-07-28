package br.com.fiap.saude.citizen.domain.port.in;

public interface SyncCitizensFromSusUseCase {

    SyncResult syncAll();

    record SyncResult(int synced, int skipped) {}
}
