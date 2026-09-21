package com.b2b.sistema.dto;

import com.b2b.sistema.model.Divergencia;

import java.time.LocalDateTime;

public class DivergenciaResponseDTO {

    private Long id;
    private String descricao;
    private LocalDateTime dataRegistro;

    public DivergenciaResponseDTO(Divergencia divergencia) {
        this.id = divergencia.getId();
        this.descricao = divergencia.getDescricao();
        this.dataRegistro = divergencia.getDataRegistro();
    }

    public Long getId() {
        return id;
    }

    public String getDescricao() {
        return descricao;
    }

    public LocalDateTime getDataRegistro() {
        return dataRegistro;
    }
}
