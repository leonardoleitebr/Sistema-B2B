package com.b2b.sistema.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * RF06 - Preco de um produto especifico dentro de uma TabelaPreco.
 * RN01 - o preco de um produto para um cliente sempre vem daqui (se existir),
 * senao cai para Produto.precoPadrao.
 */
@Entity
@Table(name = "itens_tabela_preco", uniqueConstraints = {
        @UniqueConstraint(name = "uk_item_tabela_produto", columnNames = {"tabela_preco_id", "produto_id"})
})
public class ItemTabelaPreco {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tabela_preco_id", nullable = false)
    private TabelaPreco tabelaPreco;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal preco;

    public ItemTabelaPreco() {
    }

    public ItemTabelaPreco(TabelaPreco tabelaPreco, Produto produto, BigDecimal preco) {
        this.tabelaPreco = tabelaPreco;
        this.produto = produto;
        this.preco = preco;
    }

    public Long getId() {
        return id;
    }

    public TabelaPreco getTabelaPreco() {
        return tabelaPreco;
    }

    public void setTabelaPreco(TabelaPreco tabelaPreco) {
        this.tabelaPreco = tabelaPreco;
    }

    public Produto getProduto() {
        return produto;
    }

    public void setProduto(Produto produto) {
        this.produto = produto;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public void setPreco(BigDecimal preco) {
        this.preco = preco;
    }
}
