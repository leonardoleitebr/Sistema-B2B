package com.b2b.sistema.repository;

import com.b2b.sistema.model.ItemPedido;
import com.b2b.sistema.model.StatusPedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ItemPedidoRepository extends JpaRepository<ItemPedido, Long> {

    // RF13/RF16 - historico de itens comprados por um cliente, do mais recente para o mais antigo.
    // A logica de "produto mais frequente" e "ultima quantidade comprada" e resolvida em memoria no service,
    // pois nao da para expressar as duas agregacoes (contagem e ultima quantidade) em uma unica query JPQL simples.
    List<ItemPedido> findByPedido_ClienteIdAndPedido_StatusNotOrderByPedido_DataCriacaoDesc(Long clienteId, StatusPedido statusExcluido);

    // RF15 - produtos mais vendidos no periodo (todo o sistema ou filtrado por vendedor no service)
    List<ItemPedido> findByPedido_DataCriacaoBetweenAndPedido_StatusNot(LocalDateTime inicio, LocalDateTime fim, StatusPedido statusExcluido);
}
