package com.b2b.sistema.repository;

import com.b2b.sistema.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    List<Produto> findByAtivoTrue();

    List<Produto> findByCategoriaId(Long categoriaId);

    boolean existsByCategoriaId(Long categoriaId);

    // RF15 - produtos cuja disponibilidade (estoque - reservado) ja esta no minimo configurado ou abaixo dele.
    @Query("SELECT p FROM Produto p WHERE p.ativo = true AND (p.quantidadeEstoque - p.quantidadeReservada) <= p.estoqueMinimo")
    List<Produto> buscarComEstoqueBaixo();

    List<Produto> findByDestaqueTrueAndAtivoTrue();
}
