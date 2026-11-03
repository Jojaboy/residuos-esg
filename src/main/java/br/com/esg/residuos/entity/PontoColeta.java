package br.com.esg.residuos.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pontos_coleta")
public class PontoColeta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, length = 200)
    private String endereco;

    @Column(nullable = false, length = 80)
    private String bairro;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_residuo", nullable = false, length = 20)
    private TipoResiduo tipoResiduo;

    @Column(name = "capacidade_kg", nullable = false, precision = 10, scale = 2)
    private BigDecimal capacidadeKg;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    @PrePersist
    void prePersist() {
        if (criadoEm == null) {
            criadoEm = LocalDateTime.now();
        }
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }
    public String getBairro() { return bairro; }
    public void setBairro(String bairro) { this.bairro = bairro; }
    public TipoResiduo getTipoResiduo() { return tipoResiduo; }
    public void setTipoResiduo(TipoResiduo tipoResiduo) { this.tipoResiduo = tipoResiduo; }
    public BigDecimal getCapacidadeKg() { return capacidadeKg; }
    public void setCapacidadeKg(BigDecimal capacidadeKg) { this.capacidadeKg = capacidadeKg; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
}
