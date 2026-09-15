package com.b2b.sistema.dto;

/**
 * Representacao simplificada de um perfil (Role) usada dentro do
 * UsuarioResponseDTO. Existe para evitar devolver a entidade Role
 * (ou seu proxy do Hibernate) diretamente na resposta JSON, o que
 * causa erro de serializacao quando o relacionamento e LAZY.
 */
public class RoleResumoDTO {

    private Long id;
    private String nome;

    public RoleResumoDTO(Long id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }
}
