package br.com.fiap.saude.citizen.domain.port.out;

import java.time.LocalDate;
import java.util.List;

public interface SusClientPort {

    List<SusCitizenData> fetchCitizens();

    record SusCitizenData(String cpf, String name, String email, String phone, LocalDate birthDate) {}
}
