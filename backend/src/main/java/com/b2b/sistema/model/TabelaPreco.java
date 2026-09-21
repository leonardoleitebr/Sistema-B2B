package com.b2b.sistema.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * RF06 - Tabela de precos personalizada. Agrupa precos especificos por produto
 * (ItemTabelaPreco) e pode ser vinculada a um ou mais clientes (ver Usuario.tabelaPreco).
 */
@Entity
@Table(name = "tabelas_precos")
public class TabelaPreco {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nome;

    @JsonIgnore
    @OneToMany(mappedBy = "tabelaPreco", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemTabelaPreco> itens = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "tabelaPreco")
    private List<Usuario> clientes = new ArrayList<>();

    public TabelaPreco() {
    }

    public TabelaPreco(String nome) {
        this.nome = nome;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public List<ItemTabelaPreco> getItens() {
        return itens;
    }

    public void setItens(List<ItemTabelaPreco> itens) {
        this.itens = itens;
    }

    public List<Usuario> getClientes() {
        return clientes;
    }
}
