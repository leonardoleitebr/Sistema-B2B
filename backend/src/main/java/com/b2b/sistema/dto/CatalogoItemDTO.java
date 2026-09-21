package com.b2b.sistema.dto;

import java.math.BigDecimal;

/**
 * RF07 - Item do catalogo exibido para Cliente/Vendedor, ja com o preco
 * resolvido (tabela do cliente, se houver; senao o preco padrao do produto).
 */
public class CatalogoItemDTO {

    private Long produtoId;
    private String nome;
    private String categoria;
    private String unidadeVenda;
    private BigDecimal preco;
    private boolean precoPersonalizado;
    private boolean disponivel;
    private int quantidadeDisponivel;
    private boolean destaque;

    public CatalogoItemDTO(Long produtoId, String nome, String categoria, String unidadeVenda,
                            BigDecimal preco, boolean precoPersonalizado, boolean disponivel,
                            int quantidadeDisponivel, boolean destaque) {
        this.produtoId = produtoId;
        this.nome = nome;
        this.categoria = categoria;
        this.unidadeVenda = unidadeVenda;
        this.preco = preco;
        this.precoPersonalizado = precoPersonalizado;
        this.disponivel = disponivel;
        this.quantidadeDisponivel = quantidadeDisponivel;
        this.destaque = destaque;
    }

    public Long getProdutoId() {
        return produtoId;
    }

    public String getNome() {
        return nome;
    }

    public String getCategoria() {
        return categoria;
    }

    public String getUnidadeVenda() {
        return unidadeVenda;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public boolean isPrecoPersonalizado() {
        return precoPersonalizado;
    }

    public boolean isDisponivel() {
        return disponivel;
    }

    public int getQuantidadeDisponivel() {
        return quantidadeDisponivel;
    }

    public boolean isDestaque() {
        return destaque;
    }
}
