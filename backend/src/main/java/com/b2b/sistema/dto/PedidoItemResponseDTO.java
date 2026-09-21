package com.b2b.sistema.dto;

import com.b2b.sistema.model.ItemPedido;

import java.math.BigDecimal;

public class PedidoItemResponseDTO {

    private Long produtoId;
    private String produtoNome;
    private String unidadeVenda;
    private int quantidade;
    private BigDecimal precoUnitarioAplicado;
    private BigDecimal subtotal;

    public PedidoItemResponseDTO(ItemPedido item) {
        this.produtoId = item.getProduto().getId();
        this.produtoNome = item.getProduto().getNome();
        this.unidadeVenda = item.getProduto().getUnidadeVenda();
        this.quantidade = item.getQuantidade();
        this.precoUnitarioAplicado = item.getPrecoUnitarioAplicado();
        this.subtotal = item.getSubtotal();
    }

    public Long getProdutoId() {
        return produtoId;
    }

    public String getProdutoNome() {
        return produtoNome;
    }

    public String getUnidadeVenda() {
        return unidadeVenda;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public BigDecimal getPrecoUnitarioAplicado() {
        return precoUnitarioAplicado;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }
}
