package br.com.esg.residuos.controller;

import br.com.esg.residuos.dto.IndicadoresResponse;
import br.com.esg.residuos.service.IndicadorService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/indicadores")
public class IndicadorController {

    private final IndicadorService service;

    public IndicadorController(IndicadorService service) {
        this.service = service;
    }

    @GetMapping
    public IndicadoresResponse indicadores() {
        return service.calcular();
    }
}
