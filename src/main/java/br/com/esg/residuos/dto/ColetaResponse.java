package br.com.esg.residuos.dto;

import br.com.esg.residuos.entity.Coleta;
import br.com.esg.residuos.entity.StatusColeta;
import br.com.esg.residuos.entity.TipoResiduo;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ColetaResponse(
        Long id,
        Long pontoColetaId,
        String pontoColetaNome,
        TipoResiduo tipoResiduo,
        LocalDate dataColeta,
        BigDecimal quantidadeKg,
        StatusColeta status,
        String observacao) {

    public static ColetaResponse from(Coleta c) {
        return new ColetaResponse(c.getId(), c.getPontoColeta().getId(), c.getPontoColeta().getNome(),
                c.getPontoColeta().getTipoResiduo(), c.getDataColeta(), c.getQuantidadeKg(),
                c.getStatus(), c.getObservacao());
    }
}
