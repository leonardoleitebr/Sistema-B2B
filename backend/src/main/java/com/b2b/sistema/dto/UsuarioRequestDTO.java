package com.b2b.sistema.dto;

import com.b2b.sistema.model.Role;
import com.b2b.sistema.model.Usuario;

/**
 * Dados recebidos do frontend para cadastrar ou editar um usuario.
 */
public class UsuarioRequestDTO {

    private String nome;
    private String email;
    private String senha;
    private Role perfil;

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
}
