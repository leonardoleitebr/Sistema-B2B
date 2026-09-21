package com.b2b.sistema.dto;

import com.b2b.sistema.model.TabelaPreco;

import java.util.List;
import java.util.stream.Collectors;

public class TabelaPrecoResponseDTO {

    private Long id;
    private String nome;
    private List<ItemTabelaPrecoResponseDTO> itens;

    public TabelaPrecoResponseDTO(TabelaPreco tabelaPreco) {
        this.id = tabelaPreco.getId();
        this.nome = tabelaPreco.getNome();
        this.itens = tabelaPreco.getItens().stream()
                .map(ItemTabelaPrecoResponseDTO::new)
                .collect(Collectors.toList());
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public List<ItemTabelaPrecoResponseDTO> getItens() {
        return itens;
    }
}
