package br.com.esg.residuos.service;

import br.com.esg.residuos.dto.PontoColetaRequest;
import br.com.esg.residuos.dto.PontoColetaResponse;
import br.com.esg.residuos.entity.PontoColeta;
import br.com.esg.residuos.entity.TipoResiduo;
import br.com.esg.residuos.exception.RecursoNaoEncontradoException;
import br.com.esg.residuos.exception.RegraNegocioException;
import br.com.esg.residuos.repository.ColetaRepository;
import br.com.esg.residuos.repository.PontoColetaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PontoColetaService {

    private final PontoColetaRepository pontoColetaRepository;
    private final ColetaRepository coletaRepository;

    public PontoColetaService(PontoColetaRepository pontoColetaRepository, ColetaRepository coletaRepository) {
        this.pontoColetaRepository = pontoColetaRepository;
        this.coletaRepository = coletaRepository;
    }

    @Transactional(readOnly = true)
    public List<PontoColetaResponse> listar(TipoResiduo tipo) {
        List<PontoColeta> pontos = (tipo == null)
                ? pontoColetaRepository.findAllByOrderByNomeAsc()
                : pontoColetaRepository.findByTipoResiduoOrderByNomeAsc(tipo);
        return pontos.stream().map(PontoColetaResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public PontoColetaResponse buscar(Long id) {
        return PontoColetaResponse.from(buscarEntidade(id));
    }

    @Transactional
    public PontoColetaResponse criar(PontoColetaRequest request) {
        PontoColeta ponto = new PontoColeta();
        copiar(request, ponto);
        return PontoColetaResponse.from(pontoColetaRepository.save(ponto));
    }

    @Transactional
    public PontoColetaResponse atualizar(Long id, PontoColetaRequest request) {
        PontoColeta ponto = buscarEntidade(id);
        copiar(request, ponto);
        return PontoColetaResponse.from(pontoColetaRepository.save(ponto));
    }

    @Transactional
    public void excluir(Long id) {
        PontoColeta ponto = buscarEntidade(id);
        if (coletaRepository.existsByPontoColetaId(id)) {
            throw new RegraNegocioException("Ponto de coleta possui coletas registradas e nao pode ser excluido");
        }
        pontoColetaRepository.delete(ponto);
    }

    PontoColeta buscarEntidade(Long id) {
        return pontoColetaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ponto de coleta " + id + " nao encontrado"));
    }

    private void copiar(PontoColetaRequest request, PontoColeta ponto) {
        ponto.setNome(request.nome());
        ponto.setEndereco(request.endereco());
        ponto.setBairro(request.bairro());
        ponto.setTipoResiduo(request.tipoResiduo());
        ponto.setCapacidadeKg(request.capacidadeKg());
    }
}
