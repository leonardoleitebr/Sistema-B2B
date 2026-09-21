package com.b2b.sistema.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * RF15 - Indicadores do dashboard. Para o Vendedor, os mesmos indicadores
 * sao devolvidos, mas ja filtrados para a base de clientes dele (ver
 * DashboardService); produtosEstoqueBaixo fica vazio nesse caso, pois estoque
 * e uma visao operacional/administrativa.
 */
public class DashboardResponseDTO {

    private long totalPedidos;
    private BigDecimal faturamentoTotal;
    private BigDecimal ticketMedio;
    private List<StatusContagemDTO> pedidosPorStatus;
    private List<ProdutoVendidoDTO> produtosMaisVendidos;
    private List<ProdutoResponseDTO> produtosEstoqueBaixo;
    private List<ClienteInativoDTO> clientesInativos;

    public DashboardResponseDTO(long totalPedidos, BigDecimal faturamentoTotal, BigDecimal ticketMedio,
                                 List<StatusContagemDTO> pedidosPorStatus,
                                 List<ProdutoVendidoDTO> produtosMaisVendidos,
                                 List<ProdutoResponseDTO> produtosEstoqueBaixo,
                                 List<ClienteInativoDTO> clientesInativos) {
        this.totalPedidos = totalPedidos;
        this.faturamentoTotal = faturamentoTotal;
        this.ticketMedio = ticketMedio;
        this.pedidosPorStatus = pedidosPorStatus;
        this.produtosMaisVendidos = produtosMaisVendidos;
        this.produtosEstoqueBaixo = produtosEstoqueBaixo;
        this.clientesInativos = clientesInativos;
    }

    public long getTotalPedidos() {
        return totalPedidos;
    }

    public BigDecimal getFaturamentoTotal() {
        return faturamentoTotal;
    }

    public BigDecimal getTicketMedio() {
        return ticketMedio;
    }

    public List<StatusContagemDTO> getPedidosPorStatus() {
        return pedidosPorStatus;
    }

    public List<ProdutoVendidoDTO> getProdutosMaisVendidos() {
        return produtosMaisVendidos;
    }

    public List<ProdutoResponseDTO> getProdutosEstoqueBaixo() {
        return produtosEstoqueBaixo;
    }

    public List<ClienteInativoDTO> getClientesInativos() {
        return clientesInativos;
    }
}
