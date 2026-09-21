package com.b2b.sistema.dto;

import java.math.BigDecimal;

/**
 * RF13 (comprar novamente) / RF16 (sugestoes para atingir o pedido minimo).
 */
public class ProdutoFrequenteDTO {

    private Long produtoId;
    private String nome;
    private String unidadeVenda;
    private BigDecimal preco;
    private int quantidadeUltimaCompra;
    private int vezesComprado;
    private boolean disponivel;

    public ProdutoFrequenteDTO(Long produtoId, String nome, String unidadeVenda, BigDecimal preco,
                                int quantidadeUltimaCompra, int vezesComprado, boolean disponivel) {
        this.produtoId = produtoId;
        this.nome = nome;
        this.unidadeVenda = unidadeVenda;
        this.preco = preco;
        this.quantidadeUltimaCompra = quantidadeUltimaCompra;
        this.vezesComprado = vezesComprado;
        this.disponivel = disponivel;
    }

    public Long getProdutoId() {
        return produtoId;
    }

    public String getNome() {
        return nome;
    }

    public String getUnidadeVenda() {
        return unidadeVenda;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public int getQuantidadeUltimaCompra() {
        return quantidadeUltimaCompra;
    }

    public int getVezesComprado() {
        return vezesComprado;
    }

    public boolean isDisponivel() {
        return disponivel;
    }
}
