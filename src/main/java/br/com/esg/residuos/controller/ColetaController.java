package br.com.esg.residuos.controller;

import br.com.esg.residuos.dto.ColetaRequest;
import br.com.esg.residuos.dto.ColetaResponse;
import br.com.esg.residuos.service.ColetaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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
@RequestMapping("/api/coletas")
public class ColetaController {

    private final ColetaService service;

    public ColetaController(ColetaService service) {
        this.service = service;
    }

    @GetMapping
    public List<ColetaResponse> listar(@RequestParam(required = false) Long pontoColetaId) {
        return service.listar(pontoColetaId);
    }

    @GetMapping("/{id}")
    public ColetaResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping
    public ResponseEntity<ColetaResponse> criar(@RequestBody @Valid ColetaRequest request) {
        ColetaResponse criada = service.criar(request);
        URI local = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(criada.id()).toUri();
        return ResponseEntity.created(local).body(criada);
    }

    @PutMapping("/{id}")
    public ColetaResponse atualizar(@PathVariable Long id, @RequestBody @Valid ColetaRequest request) {
        return service.atualizar(id, request);
    }

    @PatchMapping("/{id}/concluir")
    public ColetaResponse concluir(@PathVariable Long id) {
        return service.concluir(id);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
