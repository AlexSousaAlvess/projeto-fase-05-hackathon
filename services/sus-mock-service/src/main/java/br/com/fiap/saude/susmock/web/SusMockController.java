package br.com.fiap.saude.susmock.web;

import br.com.fiap.saude.susmock.SusCitizenSeedData;
import br.com.fiap.saude.susmock.model.SusCitizenRecord;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/sus")
@Tag(name = "SUS (mock)", description = "Simula a base nacional de cidadãos do SUS")
public class SusMockController {

    @GetMapping("/citizens")
    @Operation(summary = "Listar cidadãos cadastrados no SUS")
    public List<SusCitizenRecord> listCitizens() {
        return SusCitizenSeedData.CITIZENS;
    }
}
