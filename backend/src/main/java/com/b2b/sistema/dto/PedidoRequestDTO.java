package com.b2b.sistema.dto;

import java.util.List;

/**
 * RF08 - Corpo enviado pelo Cliente ao finalizar o proprio pedido.
 */
public class PedidoRequestDTO {

    private List<PedidoItemRequestDTO> itens;

    public List<PedidoItemRequestDTO> getItens() {
        return itens;
    }

    public void setItens(List<PedidoItemRequestDTO> itens) {
        this.itens = itens;
    }
}
