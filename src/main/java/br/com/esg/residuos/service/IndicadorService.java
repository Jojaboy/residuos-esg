package br.com.esg.residuos.service;

import br.com.esg.residuos.dto.IndicadoresResponse;
import br.com.esg.residuos.entity.StatusColeta;
import br.com.esg.residuos.repository.ColetaRepository;
import br.com.esg.residuos.repository.PontoColetaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IndicadorService {

    private final PontoColetaRepository pontoColetaRepository;
    private final ColetaRepository coletaRepository;

    public IndicadorService(PontoColetaRepository pontoColetaRepository, ColetaRepository coletaRepository) {
        this.pontoColetaRepository = pontoColetaRepository;
        this.coletaRepository = coletaRepository;
    }

    @Transactional(readOnly = true)
    public IndicadoresResponse calcular() {
        return new IndicadoresResponse(
                pontoColetaRepository.count(),
                coletaRepository.count(),
                coletaRepository.countByStatus(StatusColeta.AGENDADA),
                coletaRepository.countByStatus(StatusColeta.CONCLUIDA),
                coletaRepository.somarQuantidadePorStatus(StatusColeta.CONCLUIDA));
    }
}
