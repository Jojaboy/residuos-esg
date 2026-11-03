package br.com.esg.residuos.repository;

import br.com.esg.residuos.entity.Coleta;
import br.com.esg.residuos.entity.StatusColeta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface ColetaRepository extends JpaRepository<Coleta, Long> {

    List<Coleta> findAllByOrderByDataColetaDesc();

    List<Coleta> findByPontoColetaIdOrderByDataColetaDesc(Long pontoColetaId);

    long countByStatus(StatusColeta status);

    boolean existsByPontoColetaId(Long pontoColetaId);

    @Query("select coalesce(sum(c.quantidadeKg), 0) from Coleta c where c.status = :status")
    BigDecimal somarQuantidadePorStatus(@Param("status") StatusColeta status);
}
