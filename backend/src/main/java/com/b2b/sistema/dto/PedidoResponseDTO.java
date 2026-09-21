package com.b2b.sistema.dto;

import com.b2b.sistema.model.Pedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class PedidoResponseDTO {

    private Long id;
    private UsuarioResumoDTO cliente;
    private UsuarioResumoDTO vendedor;
    private String status;
    private BigDecimal valorTotal;
    private BigDecimal descontoPercentual;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataAtualizacao;
    private List<PedidoItemResponseDTO> itens;
    private List<PedidoStatusHistoricoResponseDTO> historico;
    private List<DivergenciaResponseDTO> divergencias;

    public PedidoResponseDTO(Pedido pedido) {
        this.id = pedido.getId();
        this.cliente = new UsuarioResumoDTO(pedido.getCliente());
        this.vendedor = pedido.getVendedor() != null ? new UsuarioResumoDTO(pedido.getVendedor()) : null;
        this.status = pedido.getStatus().name();
        this.valorTotal = pedido.getValorTotal();
        this.descontoPercentual = pedido.getDescontoPercentual();
        this.dataCriacao = pedido.getDataCriacao();
        this.dataAtualizacao = pedido.getDataAtualizacao();
        this.itens = pedido.getItens().stream().map(PedidoItemResponseDTO::new).collect(Collectors.toList());
        this.historico = pedido.getHistorico().stream()
                .map(PedidoStatusHistoricoResponseDTO::new).collect(Collectors.toList());
        this.divergencias = pedido.getDivergencias().stream()
                .map(DivergenciaResponseDTO::new).collect(Collectors.toList());
    }

    public Long getId() {
        return id;
    }

    public UsuarioResumoDTO getCliente() {
        return cliente;
    }

    public UsuarioResumoDTO getVendedor() {
        return vendedor;
    }

    public String getStatus() {
        return status;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public BigDecimal getDescontoPercentual() {
        return descontoPercentual;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public LocalDateTime getDataAtualizacao() {
        return dataAtualizacao;
    }

    public List<PedidoItemResponseDTO> getItens() {
        return itens;
    }

    public List<PedidoStatusHistoricoResponseDTO> getHistorico() {
        return historico;
    }

    public List<DivergenciaResponseDTO> getDivergencias() {
        return divergencias;
    }
}
