package com.b2b.sistema.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * RF10 - Corpo enviado pelo Vendedor ao lancar um pedido em nome de um cliente da sua base.
 */
public class PedidoVendedorRequestDTO {

    private Long clienteId;
    private List<PedidoItemRequestDTO> itens;
    private BigDecimal descontoPercentual;

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public List<PedidoItemRequestDTO> getItens() {
        return itens;
    }

    public void setItens(List<PedidoItemRequestDTO> itens) {
        this.itens = itens;
    }

    public BigDecimal getDescontoPercentual() {
        return descontoPercentual;
    }

    public void setDescontoPercentual(BigDecimal descontoPercentual) {
        this.descontoPercentual = descontoPercentual;
    }
}
