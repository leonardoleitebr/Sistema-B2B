package com.b2b.sistema.dto;

import java.math.BigDecimal;

/**
 * Dados recebidos do frontend para cadastrar ou editar um produto (RF04).
 */
public class ProdutoRequestDTO {

    private String nome;
    private Long categoriaId;
    private String unidadeVenda;
    private BigDecimal precoPadrao;
    private Integer quantidadeEstoque;
    private Integer estoqueMinimo;
    private boolean destaque;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Long getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(Long categoriaId) {
        this.categoriaId = categoriaId;
    }

    public String getUnidadeVenda() {
        return unidadeVenda;
    }

    public void setUnidadeVenda(String unidadeVenda) {
        this.unidadeVenda = unidadeVenda;
    }

    public BigDecimal getPrecoPadrao() {
        return precoPadrao;
    }

    public void setPrecoPadrao(BigDecimal precoPadrao) {
        this.precoPadrao = precoPadrao;
    }

    public Integer getQuantidadeEstoque() {
        return quantidadeEstoque;
    }

    public void setQuantidadeEstoque(Integer quantidadeEstoque) {
        this.quantidadeEstoque = quantidadeEstoque;
    }

    public Integer getEstoqueMinimo() {
        return estoqueMinimo;
    }

    public void setEstoqueMinimo(Integer estoqueMinimo) {
        this.estoqueMinimo = estoqueMinimo;
    }

    public boolean isDestaque() {
        return destaque;
    }

    public void setDestaque(boolean destaque) {
        this.destaque = destaque;
    }
}
