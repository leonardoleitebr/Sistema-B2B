package com.b2b.sistema.model;

/**
 * Fluxo oficial de status do pedido, conforme definido na devolutiva do professor
 * (RF11/RF14): RASCUNHO -> AGUARDANDO_APROVACAO -> APROVADO -> EM_SEPARACAO ->
 * (DIVERGENCIA) -> ENVIADO -> CONCLUIDO, com CANCELADO como saida em qualquer ponto
 * permitido.
 */
public enum StatusPedido {
    RASCUNHO,
    AGUARDANDO_APROVACAO,
    APROVADO,
    EM_SEPARACAO,
    DIVERGENCIA,
    ENVIADO,
    CONCLUIDO,
    CANCELADO
}
