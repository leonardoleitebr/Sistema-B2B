package com.b2b.sistema.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * RF04 - Cadastro e gestao de produtos.
 * RF05 - Controle de estoque: quantidadeEstoque e o total fisico; quantidadeReservada
 * e a parte ja comprometida com pedidos aprovados (mas ainda nao enviados). A
 * disponibilidade real para venda e sempre (quantidadeEstoque - quantidadeReservada).
 */
@Entity
@Table(name = "produtos")
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @Column(name = "unidade_venda", nullable = false)
    private String unidadeVenda;

    @Column(name = "preco_padrao", nullable = false, precision = 12, scale = 2)
    private BigDecimal precoPadrao;

    @Column(name = "quantidade_estoque", nullable = false)
    private int quantidadeEstoque;

    @Column(name = "quantidade_reservada", nullable = false)
    private int quantidadeReservada;

    @Column(name = "estoque_minimo", nullable = false)
    private int estoqueMinimo;

    @Column(nullable = false)
    private boolean destaque;

    @Column(nullable = false)
    private boolean ativo;

    public Produto() {
    }

    /** RF05 - disponibilidade real de venda (nunca negativa). */
    @Transient
    public int getQuantidadeDisponivel() {
        int disponivel = quantidadeEstoque - quantidadeReservada;
        return Math.max(disponivel, 0);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
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

    public int getQuantidadeEstoque() {
        return quantidadeEstoque;
    }

    public void setQuantidadeEstoque(int quantidadeEstoque) {
        this.quantidadeEstoque = quantidadeEstoque;
    }

    public int getQuantidadeReservada() {
        return quantidadeReservada;
    }

    public void setQuantidadeReservada(int quantidadeReservada) {
        this.quantidadeReservada = quantidadeReservada;
    }

    public int getEstoqueMinimo() {
        return estoqueMinimo;
    }

    public void setEstoqueMinimo(int estoqueMinimo) {
        this.estoqueMinimo = estoqueMinimo;
    }

    public boolean isDestaque() {
        return destaque;
    }

    public void setDestaque(boolean destaque) {
        this.destaque = destaque;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
}
