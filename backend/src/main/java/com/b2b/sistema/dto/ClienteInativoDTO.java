package com.b2b.sistema.dto;

import java.time.LocalDateTime;

public class ClienteInativoDTO {

    private Long clienteId;
    private String nome;
    private LocalDateTime ultimaCompra;

    public ClienteInativoDTO(Long clienteId, String nome, LocalDateTime ultimaCompra) {
        this.clienteId = clienteId;
        this.nome = nome;
        this.ultimaCompra = ultimaCompra;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public String getNome() {
        return nome;
    }

    public LocalDateTime getUltimaCompra() {
        return ultimaCompra;
    }
}
