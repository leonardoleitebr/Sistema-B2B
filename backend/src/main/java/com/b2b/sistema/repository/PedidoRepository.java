package com.b2b.sistema.repository;

import com.b2b.sistema.model.Pedido;
import com.b2b.sistema.model.StatusPedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    // RF11 - pedidos de um cliente (historico do proprio cliente)
    List<Pedido> findByClienteIdOrderByDataCriacaoDesc(Long clienteId);

    // RF10 - pedidos lancados diretamente por um vendedor
    List<Pedido> findByVendedorIdOrderByDataCriacaoDesc(Long vendedorId);

    // Pedidos dos clientes atendidos por um vendedor (mesmo os que o proprio cliente lancou)
    List<Pedido> findByCliente_VendedorResponsavel_IdOrderByDataCriacaoDesc(Long vendedorId);

    // RF14 - pedidos que ja passaram por aprovacao (visiveis para a Expedicao)
    List<Pedido> findByStatusInOrderByDataCriacaoDesc(List<StatusPedido> status);

    List<Pedido> findAllByOrderByDataCriacaoDesc();

    // RF15 - base para os indicadores do dashboard
    List<Pedido> findByDataCriacaoBetweenAndStatusNot(LocalDateTime inicio, LocalDateTime fim, StatusPedido statusExcluido);

    List<Pedido> findByDataCriacaoBetweenAndStatusNotAndCliente_VendedorResponsavel_Id(
            LocalDateTime inicio, LocalDateTime fim, StatusPedido statusExcluido, Long vendedorId);
}
