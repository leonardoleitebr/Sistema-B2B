package com.b2b.sistema.dto;

import com.b2b.sistema.model.ItemTabelaPreco;

import java.math.BigDecimal;

public class ItemTabelaPrecoResponseDTO {

    private Long produtoId;
    private String produtoNome;
    private BigDecimal preco;

    public ItemTabelaPrecoResponseDTO(ItemTabelaPreco item) {
        this.produtoId = item.getProduto().getId();
        this.produtoNome = item.getProduto().getNome();
        this.preco = item.getPreco();
    }

    public Long getProdutoId() {
        return produtoId;
    }

    public String getProdutoNome() {
        return produtoNome;
    }

    public BigDecimal getPreco() {
        return preco;
    }
}
