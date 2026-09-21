package com.b2b.sistema.dto;

import com.b2b.sistema.model.Usuario;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Dados devolvidos ao frontend. Nunca inclui a senha (nem mesmo o hash).
 *
 * O perfil e copiado para um RoleResumoDTO (em vez de expor a entidade
 * Role/seu proxy do Hibernate direto), o que evita erro de serializacao
 * do Jackson com relacionamentos LAZY. O mesmo vale para tabelaPreco e
 * vendedorResponsavel.
 */
public class UsuarioResponseDTO {

    private Long id;
    private String nome;
    private String email;
    private RoleResumoDTO perfil;
    private boolean ativo;
    private LocalDateTime dataCriacao;
    private Long tabelaPrecoId;
    private String tabelaPrecoNome;
    private UsuarioResumoDTO vendedorResponsavel;
    private BigDecimal limiteDescontoPercentual;

    public UsuarioResponseDTO(Usuario usuario) {
        this.id = usuario.getId();
        this.nome = usuario.getNome();
        this.email = usuario.getEmail();
        this.ativo = usuario.isAtivo();
        this.dataCriacao = usuario.getDataCriacao();

        if (usuario.getRole() != null) {
            this.perfil = new RoleResumoDTO(usuario.getRole().getId(), usuario.getRole().getNome());
        }
        if (usuario.getTabelaPreco() != null) {
            this.tabelaPrecoId = usuario.getTabelaPreco().getId();
            this.tabelaPrecoNome = usuario.getTabelaPreco().getNome();
        }
        if (usuario.getVendedorResponsavel() != null) {
            this.vendedorResponsavel = new UsuarioResumoDTO(usuario.getVendedorResponsavel());
        }
        this.limiteDescontoPercentual = usuario.getLimiteDescontoPercentual();
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public RoleResumoDTO getPerfil() {
        return perfil;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public Long getTabelaPrecoId() {
        return tabelaPrecoId;
    }

    public String getTabelaPrecoNome() {
        return tabelaPrecoNome;
    }

    public UsuarioResumoDTO getVendedorResponsavel() {
        return vendedorResponsavel;
    }

    public BigDecimal getLimiteDescontoPercentual() {
        return limiteDescontoPercentual;
    }
}
