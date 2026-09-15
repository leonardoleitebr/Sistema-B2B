package com.b2b.sistema.dto;

/**
 * Corpo esperado por POST /api/auth/login (RF02 - Autenticacao e
 * controle de acesso por perfil).
 */
public class LoginRequestDTO {

    private String email;
    private String senha;

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
}
