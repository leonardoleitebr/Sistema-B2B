package com.b2b.sistema.exception;

/**
 * Excecao lancada quando uma regra de negocio do RF01 e violada
 * (ex.: e-mail duplicado, usuario inativo tentando logar, etc).
 */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
