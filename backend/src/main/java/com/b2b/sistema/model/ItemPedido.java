package com.b2b.sistema.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * RF08 - Item dentro de um pedido. O preco unitario aplicado fica congelado no
 * momento da criacao do pedido (nao muda se a tabela de precos for alterada depois).
 */
@Entity
@Table(name = "itens_pedido")
public class ItemPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @Column(nullable = false)
    private int quantidade;

    @Column(name = "preco_unitario_aplicado", nullable = false, precision = 12, scale = 2)
    private BigDecimal precoUnitarioAplicado;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    public ItemPedido() {
    }

    public ItemPedido(Pedido pedido, Produto produto, int quantidade, BigDecimal precoUnitarioAplicado) {
        this.pedido = pedido;
        this.produto = produto;
        this.quantidade = quantidade;
        this.precoUnitarioAplicado = precoUnitarioAplicado;
        this.subtotal = precoUnitarioAplicado.multiply(BigDecimal.valueOf(quantidade));
    }

    public Long getId() {
        return id;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }

    public Produto getProduto() {
        return produto;
    }

    public void setProduto(Produto produto) {
        this.produto = produto;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    public BigDecimal getPrecoUnitarioAplicado() {
        return precoUnitarioAplicado;
    }

    public void setPrecoUnitarioAplicado(BigDecimal precoUnitarioAplicado) {
        this.precoUnitarioAplicado = precoUnitarioAplicado;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }
}
