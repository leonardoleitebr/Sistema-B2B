package com.b2b.sistema.dto;

import java.math.BigDecimal;

public class ConfiguracaoRequestDTO {

    private BigDecimal valorPedidoMinimo;
    private Integer diasInatividadeCliente;

    public BigDecimal getValorPedidoMinimo() {
        return valorPedidoMinimo;
    }

    public void setValorPedidoMinimo(BigDecimal valorPedidoMinimo) {
        this.valorPedidoMinimo = valorPedidoMinimo;
    }

    public Integer getDiasInatividadeCliente() {
        return diasInatividadeCliente;
    }

    public void setDiasInatividadeCliente(Integer diasInatividadeCliente) {
        this.diasInatividadeCliente = diasInatividadeCliente;
    }
}
