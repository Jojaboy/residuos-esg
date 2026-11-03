package br.com.esg.residuos.service;

import br.com.esg.residuos.dto.ColetaRequest;
import br.com.esg.residuos.dto.ColetaResponse;
import br.com.esg.residuos.entity.Coleta;
import br.com.esg.residuos.entity.StatusColeta;
import br.com.esg.residuos.exception.RecursoNaoEncontradoException;
import br.com.esg.residuos.exception.RegraNegocioException;
import br.com.esg.residuos.repository.ColetaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ColetaService {

    private final ColetaRepository coletaRepository;
    private final PontoColetaService pontoColetaService;

    public ColetaService(ColetaRepository coletaRepository, PontoColetaService pontoColetaService) {
        this.coletaRepository = coletaRepository;
        this.pontoColetaService = pontoColetaService;
    }

    @Transactional(readOnly = true)
    public List<ColetaResponse> listar(Long pontoColetaId) {
        List<Coleta> coletas = (pontoColetaId == null)
                ? coletaRepository.findAllByOrderByDataColetaDesc()
                : coletaRepository.findByPontoColetaIdOrderByDataColetaDesc(pontoColetaId);
        return coletas.stream().map(ColetaResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public ColetaResponse buscar(Long id) {
        return ColetaResponse.from(buscarEntidade(id));
    }

    @Transactional
    public ColetaResponse criar(ColetaRequest request) {
        Coleta coleta = new Coleta();
        coleta.setStatus(StatusColeta.AGENDADA);
        copiar(request, coleta);
        return ColetaResponse.from(coletaRepository.save(coleta));
    }

    @Transactional
    public ColetaResponse atualizar(Long id, ColetaRequest request) {
        Coleta coleta = buscarEntidade(id);
        if (coleta.getStatus() != StatusColeta.AGENDADA) {
            throw new RegraNegocioException("Somente coletas AGENDADAS podem ser alteradas");
        }
        copiar(request, coleta);
        return ColetaResponse.from(coletaRepository.save(coleta));
    }

    @Transactional
    public ColetaResponse concluir(Long id) {
        Coleta coleta = buscarEntidade(id);
        if (coleta.getStatus() != StatusColeta.AGENDADA) {
            throw new RegraNegocioException("Somente coletas AGENDADAS podem ser concluidas");
        }
        coleta.setStatus(StatusColeta.CONCLUIDA);
        return ColetaResponse.from(coletaRepository.save(coleta));
    }

    @Transactional
    public void excluir(Long id) {
        coletaRepository.delete(buscarEntidade(id));
    }

    private Coleta buscarEntidade(Long id) {
        return coletaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Coleta " + id + " nao encontrada"));
    }

    private void copiar(ColetaRequest request, Coleta coleta) {
        coleta.setPontoColeta(pontoColetaService.buscarEntidade(request.pontoColetaId()));
        coleta.setDataColeta(request.dataColeta());
        coleta.setQuantidadeKg(request.quantidadeKg());
        coleta.setObservacao(request.observacao());
    }
}
