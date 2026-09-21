package com.b2b.sistema.repository;

import com.b2b.sistema.model.TabelaPreco;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TabelaPrecoRepository extends JpaRepository<TabelaPreco, Long> {

    boolean existsByNome(String nome);

    boolean existsByNomeAndIdNot(String nome, Long id);
}
