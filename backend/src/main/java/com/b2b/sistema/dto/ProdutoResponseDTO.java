package com.b2b.sistema.dto;

import com.b2b.sistema.model.Produto;

import java.math.BigDecimal;

public class ProdutoResponseDTO {

    private Long id;
    private String nome;
    private CategoriaResumoDTO categoria;
    private String unidadeVenda;
    private BigDecimal precoPadrao;
    private int quantidadeEstoque;
    private int quantidadeReservada;
    private int quantidadeDisponivel;
    private int estoqueMinimo;
    private boolean estoqueBaixo;
    private boolean destaque;
    private boolean ativo;

    public ProdutoResponseDTO(Produto produto) {
        this.id = produto.getId();
        this.nome = produto.getNome();
        this.categoria = new CategoriaResumoDTO(produto.getCategoria());
        this.unidadeVenda = produto.getUnidadeVenda();
        this.precoPadrao = produto.getPrecoPadrao();
        this.quantidadeEstoque = produto.getQuantidadeEstoque();
        this.quantidadeReservada = produto.getQuantidadeReservada();
        this.quantidadeDisponivel = produto.getQuantidadeDisponivel();
        this.estoqueMinimo = produto.getEstoqueMinimo();
        this.estoqueBaixo = produto.getQuantidadeDisponivel() <= produto.getEstoqueMinimo();
        this.destaque = produto.isDestaque();
        this.ativo = produto.isAtivo();
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public CategoriaResumoDTO getCategoria() {
        return categoria;
    }

    public String getUnidadeVenda() {
        return unidadeVenda;
    }

    public BigDecimal getPrecoPadrao() {
        return precoPadrao;
    }

    public int getQuantidadeEstoque() {
        return quantidadeEstoque;
    }

    public int getQuantidadeReservada() {
        return quantidadeReservada;
    }

    public int getQuantidadeDisponivel() {
        return quantidadeDisponivel;
    }

    public int getEstoqueMinimo() {
        return estoqueMinimo;
    }

    public boolean isEstoqueBaixo() {
        return estoqueBaixo;
    }

    public boolean isDestaque() {
        return destaque;
    }

    public boolean isAtivo() {
        return ativo;
    }
}
