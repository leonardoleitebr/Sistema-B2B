package com.b2b.sistema.dto;

import com.b2b.sistema.model.PedidoStatusHistorico;

import java.time.LocalDateTime;

public class PedidoStatusHistoricoResponseDTO {

    private String status;
    private LocalDateTime dataHora;
    private String observacao;

    public PedidoStatusHistoricoResponseDTO(PedidoStatusHistorico historico) {
        this.status = historico.getStatus().name();
        this.dataHora = historico.getDataHora();
        this.observacao = historico.getObservacao();
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public String getObservacao() {
        return observacao;
    }
}
