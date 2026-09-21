package com.b2b.sistema.dto;

import com.b2b.sistema.model.Categoria;

public class CategoriaResumoDTO {

    private Long id;
    private String nome;

    public CategoriaResumoDTO(Categoria categoria) {
        if (categoria == null) {
            return;
        }
        this.id = categoria.getId();
        this.nome = categoria.getNome();
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }
}
