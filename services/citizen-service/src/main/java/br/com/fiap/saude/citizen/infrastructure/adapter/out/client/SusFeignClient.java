package br.com.fiap.saude.citizen.infrastructure.adapter.out.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "sus-mock-service")
public interface SusFeignClient {

    @GetMapping("/sus/citizens")
    List<SusCitizenDto> fetchCitizens();
}
