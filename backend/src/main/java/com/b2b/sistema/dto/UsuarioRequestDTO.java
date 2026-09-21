package com.b2b.sistema.dto;

import com.b2b.sistema.model.Role;
import com.b2b.sistema.model.Usuario;

import java.math.BigDecimal;

/**
 * Dados recebidos do frontend para cadastrar ou editar um usuario.
 *
 * tabelaPrecoId e vendedorResponsavelId so fazem sentido quando perfil = ROLE_CLIENTE;
 * limiteDescontoPercentual so faz sentido quando perfil = ROLE_VENDEDOR (RF10). O
 * frontend so exibe/envia o campo relevante para o perfil escolhido; o backend apenas
 * valida a integridade referencial (ver UsuarioService).
 */
public class UsuarioRequestDTO {

    private String nome;
    private String email;
    private String senha;
    private Role perfil;
    private Long tabelaPrecoId;
    private Long vendedorResponsavelId;
    private BigDecimal limiteDescontoPercentual;

    public Usuario paraEntidade() {
        Usuario usuario = new Usuario();
        usuario.setNome(this.nome);
        usuario.setEmail(this.email);
        usuario.setSenha(this.senha);
        usuario.setRole(this.perfil);
        return usuario;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public Role getPerfil() {
        return perfil;
    }

    public void setPerfil(Role perfil) {
        this.perfil = perfil;
    }

    public Long getTabelaPrecoId() {
        return tabelaPrecoId;
    }

    public void setTabelaPrecoId(Long tabelaPrecoId) {
        this.tabelaPrecoId = tabelaPrecoId;
    }

    public Long getVendedorResponsavelId() {
        return vendedorResponsavelId;
    }

    public void setVendedorResponsavelId(Long vendedorResponsavelId) {
        this.vendedorResponsavelId = vendedorResponsavelId;
    }

    public BigDecimal getLimiteDescontoPercentual() {
        return limiteDescontoPercentual;
    }

    public void setLimiteDescontoPercentual(BigDecimal limiteDescontoPercentual) {
        this.limiteDescontoPercentual = limiteDescontoPercentual;
    }
}
