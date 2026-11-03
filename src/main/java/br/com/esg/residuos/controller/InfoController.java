package br.com.esg.residuos.controller;

import br.com.esg.residuos.dto.InfoResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * Endpoint publico que mostra em qual ambiente (local, staging ou production)
 * a aplicacao esta rodando. Usado como evidencia nos deploys do pipeline.
 */
@RestController
@RequestMapping("/api/info")
public class InfoController {

    private final String aplicacao;
    private final String versao;
    private final String ambiente;

    public InfoController(@Value("${spring.application.name}") String aplicacao,
                          @Value("${app.version}") String versao,
                          @Value("${app.environment}") String ambiente) {
        this.aplicacao = aplicacao;
        this.versao = versao;
        this.ambiente = ambiente;
    }

    @GetMapping
    public InfoResponse info() {
        return new InfoResponse(aplicacao, versao, ambiente, LocalDateTime.now());
    }
}
