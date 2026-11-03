package br.com.esg.residuos.controller;

import br.com.esg.residuos.dto.PontoColetaRequest;
import br.com.esg.residuos.dto.PontoColetaResponse;
import br.com.esg.residuos.entity.TipoResiduo;
import br.com.esg.residuos.service.PontoColetaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/pontos-coleta")
public class PontoColetaController {

    private final PontoColetaService service;

    public PontoColetaController(PontoColetaService service) {
        this.service = service;
    }

    @GetMapping
    public List<PontoColetaResponse> listar(@RequestParam(required = false) TipoResiduo tipo) {
        return service.listar(tipo);
    }

    @GetMapping("/{id}")
    public PontoColetaResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping
    public ResponseEntity<PontoColetaResponse> criar(@RequestBody @Valid PontoColetaRequest request) {
        PontoColetaResponse criado = service.criar(request);
        URI local = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(criado.id()).toUri();
        return ResponseEntity.created(local).body(criado);
    }

    @PutMapping("/{id}")
    public PontoColetaResponse atualizar(@PathVariable Long id, @RequestBody @Valid PontoColetaRequest request) {
        return service.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
