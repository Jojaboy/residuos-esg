package br.com.esg.residuos.dto;

import java.math.BigDecimal;

public record IndicadoresResponse(
        long totalPontosColeta,
        long totalColetas,
        long coletasAgendadas,
        long coletasConcluidas,
        BigDecimal totalKgReciclado) {
}
