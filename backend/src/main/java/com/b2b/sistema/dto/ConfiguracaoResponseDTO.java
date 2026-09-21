package com.b2b.sistema.dto;

import com.b2b.sistema.model.Configuracao;

import java.math.BigDecimal;

public class ConfiguracaoResponseDTO {

    private BigDecimal valorPedidoMinimo;
    private int diasInatividadeCliente;

    public ConfiguracaoResponseDTO(Configuracao configuracao) {
        this.valorPedidoMinimo = configuracao.getValorPedidoMinimo();
        this.diasInatividadeCliente = configuracao.getDiasInatividadeCliente();
    }

    public BigDecimal getValorPedidoMinimo() {
        return valorPedidoMinimo;
    }

    public int getDiasInatividadeCliente() {
        return diasInatividadeCliente;
    }
}
