package com.b2b.sistema.dto;

/**
 * Corpo esperado por POST /api/auth/esqueci-senha.
 */
public class EsqueciSenhaRequestDTO {

    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
