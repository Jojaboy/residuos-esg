package br.com.esg.residuos.repository;

import br.com.esg.residuos.entity.PontoColeta;
import br.com.esg.residuos.entity.TipoResiduo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PontoColetaRepository extends JpaRepository<PontoColeta, Long> {
    List<PontoColeta> findByTipoResiduoOrderByNomeAsc(TipoResiduo tipoResiduo);
    List<PontoColeta> findAllByOrderByNomeAsc();
}
