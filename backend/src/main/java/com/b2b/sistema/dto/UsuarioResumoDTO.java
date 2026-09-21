package com.b2b.sistema.dto;

import com.b2b.sistema.model.Usuario;

/**
 * Representacao simplificada de um Usuario (sem senha) usada para aninhar
 * cliente/vendedor dentro de outras respostas (ex.: PedidoResponseDTO).
 */
public class UsuarioResumoDTO {

    private Long id;
    private String nome;
    private String email;

    public UsuarioResumoDTO(Usuario usuario) {
        if (usuario == null) {
            return;
        }
        this.id = usuario.getId();
        this.nome = usuario.getNome();
        this.email = usuario.getEmail();
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
}
