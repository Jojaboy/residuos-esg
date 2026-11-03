package br.com.esg.residuos.dto;

import br.com.esg.residuos.entity.PontoColeta;
import br.com.esg.residuos.entity.TipoResiduo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PontoColetaResponse(
        Long id,
        String nome,
        String endereco,
        String bairro,
        TipoResiduo tipoResiduo,
        BigDecimal capacidadeKg,
        LocalDateTime criadoEm) {

    public static PontoColetaResponse from(PontoColeta p) {
        return new PontoColetaResponse(p.getId(), p.getNome(), p.getEndereco(), p.getBairro(),
                p.getTipoResiduo(), p.getCapacidadeKg(), p.getCriadoEm());
    }
}
