package com.b2b.sistema.dto;

import com.b2b.sistema.model.Usuario;

/**
 * Resposta do endpoint de login: o token que o frontend deve enviar
 * nas proximas requisicoes (header Authorization: Bearer TOKEN) e os
 * dados do usuario logado (sem a senha).
 */
public class LoginResponseDTO {

    private String token;
    private UsuarioResponseDTO usuario;

    public LoginResponseDTO(String token, Usuario usuario) {
        this.token = token;
        this.usuario = new UsuarioResponseDTO(usuario);
    }

    public String getToken() {
        return token;
    }

    public UsuarioResponseDTO getUsuario() {
        return usuario;
    }
}
