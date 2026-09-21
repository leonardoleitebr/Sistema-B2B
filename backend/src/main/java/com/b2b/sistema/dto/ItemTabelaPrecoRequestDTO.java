package com.b2b.sistema.dto;

import java.math.BigDecimal;

public class ItemTabelaPrecoRequestDTO {

    private Long produtoId;
    private BigDecimal preco;

    public Long getProdutoId() {
        return produtoId;
    }

    public void setProdutoId(Long produtoId) {
        this.produtoId = produtoId;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public void setPreco(BigDecimal preco) {
        this.preco = preco;
    }
}
