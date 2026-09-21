package com.b2b.sistema.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * RF09 R1 - "O valor de pedido minimo deve ser configuravel pelo Administrador."
 * RF15 - "sinalizar clientes que nao compram ha mais de um numero definido de dias."
 * Linha unica (id fixo = 1) com as constantes de negocio ajustaveis pelo Admin.
 */
@Entity
@Table(name = "configuracoes")
public class Configuracao {

    @Id
    private Long id = 1L;

    @Column(name = "valor_pedido_minimo", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorPedidoMinimo;

    @Column(name = "dias_inatividade_cliente", nullable = false)
    private int diasInatividadeCliente;

    public Configuracao() {
    }

    public Configuracao(BigDecimal valorPedidoMinimo, int diasInatividadeCliente) {
        this.id = 1L;
        this.valorPedidoMinimo = valorPedidoMinimo;
        this.diasInatividadeCliente = diasInatividadeCliente;
    }

    public Long getId() {
        return id;
    }

    public BigDecimal getValorPedidoMinimo() {
        return valorPedidoMinimo;
    }

    public void setValorPedidoMinimo(BigDecimal valorPedidoMinimo) {
        this.valorPedidoMinimo = valorPedidoMinimo;
    }

    public int getDiasInatividadeCliente() {
        return diasInatividadeCliente;
    }

    public void setDiasInatividadeCliente(int diasInatividadeCliente) {
        this.diasInatividadeCliente = diasInatividadeCliente;
    }
}
