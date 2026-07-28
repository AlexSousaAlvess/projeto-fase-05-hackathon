package br.com.fiap.saude.citizen.infrastructure.adapter.out.client;

import br.com.fiap.saude.citizen.domain.port.out.SusClientPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SusClientAdapter implements SusClientPort {

    private final SusFeignClient feignClient;

    @Override
    public List<SusCitizenData> fetchCitizens() {
        return feignClient.fetchCitizens().stream()
                .map(dto -> new SusCitizenData(dto.cpf(), dto.name(), dto.email(), dto.phone(), dto.birthDate()))
                .toList();
    }
}
