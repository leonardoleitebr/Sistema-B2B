package com.b2b.sistema.dto;

public class StatusContagemDTO {

    private String status;
    private long quantidade;

    public StatusContagemDTO(String status, long quantidade) {
        this.status = status;
        this.quantidade = quantidade;
    }

    public String getStatus() {
        return status;
    }

    public long getQuantidade() {
        return quantidade;
    }
}
