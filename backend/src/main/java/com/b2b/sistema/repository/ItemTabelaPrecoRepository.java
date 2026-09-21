package com.b2b.sistema.repository;

import com.b2b.sistema.model.ItemTabelaPreco;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ItemTabelaPrecoRepository extends JpaRepository<ItemTabelaPreco, Long> {

    Optional<ItemTabelaPreco> findByTabelaPrecoIdAndProdutoId(Long tabelaPrecoId, Long produtoId);
}
